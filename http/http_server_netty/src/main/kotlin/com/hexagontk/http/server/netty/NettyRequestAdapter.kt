package com.hexagontk.http.server.netty

import com.hexagontk.http.model.*
import com.hexagontk.http.parseContentType
import io.netty.buffer.ByteBufHolder
import io.netty.buffer.ByteBufUtil
import io.netty.buffer.Unpooled
import io.netty.channel.Channel
import io.netty.handler.codec.http.HttpHeaderNames.*
import io.netty.handler.codec.http.HttpHeaders
import io.netty.handler.codec.http.QueryStringDecoder
import io.netty.handler.codec.http.cookie.Cookie
import io.netty.handler.codec.http.cookie.ServerCookieDecoder
import io.netty.handler.codec.http.multipart.*
import io.netty.handler.codec.http.HttpRequest
import io.netty.handler.codec.http.cookie.CookieHeaderNames.SameSite.*
import io.netty.handler.codec.http.cookie.DefaultCookie
import java.net.InetSocketAddress
import java.net.URI
import java.security.cert.X509Certificate
import kotlin.Long.Companion.MIN_VALUE
import io.netty.handler.codec.http.HttpMethod as NettyHttpMethod

class NettyRequestAdapter(
    methodName: NettyHttpMethod,
    req: HttpRequest,
    override val certificateChain: List<X509Certificate>,
    channel: Channel,
    nettyHeaders: HttpHeaders,
) : HttpRequestPort {

    private val address: InetSocketAddress by lazy { channel.remoteAddress() as InetSocketAddress }

    override val accept: List<ContentType> by lazy {
        nettyHeaders.getAll(ACCEPT).flatMap { it.split(",") }.map { parseContentType(it) }
    }

    override val contentLength: Long by lazy {
        nettyHeaders[CONTENT_LENGTH]?.toLong() ?: 0L
    }

    override val queryParameters: Parameters by lazy {
        val queryStringDecoder = QueryStringDecoder(req.uri())
        Parameters(
            queryStringDecoder.parameters().flatMap { (k, v) -> v.map { Field(k, it) } }
        )
    }

    override val parts: List<HttpPart> by lazy {
        HttpPostRequestDecoder(req).bodyHttpDatas.map {
            when (it) {
                is FileUpload -> HttpPart(
                    name = it.name,
                    body = ByteBufUtil.getBytes(it.content()),
                    submittedFileName = it.filename,
                    contentType = it.contentType?.let { ct -> parseContentType(ct) },
                )
                is Attribute -> HttpPart(it.name, it.value)
                else -> error("Unknown part type: ${it.javaClass}")
            }
        }
    }

    override val formParameters: Parameters by lazy {
        val fields = parts
            .filter { it.submittedFileName == null }
            .map { Field(it.name, it.bodyString()) }

        Parameters(fields)
    }

    override val method: HttpMethod by lazy {
        HttpMethod.valueOf(methodName.name())
    }

    override val protocol: HttpProtocol by lazy {
        HttpProtocol.valueOf(req.protocolVersion().protocolName())
    }

    override val host: String by lazy {
        address.hostName
    }

    override val port: Int by lazy {
        address.port
    }

    override val path: String by lazy { URI(req.uri()).path }

    override val cookies: List<com.hexagontk.http.model.Cookie> by lazy {
        val cookieHeader: String = nettyHeaders.get(COOKIE)
            ?: return@lazy emptyList<com.hexagontk.http.model.Cookie>()

        val cookies: Set<Cookie> = ServerCookieDecoder.STRICT.decode(cookieHeader)

        cookies.map {
            Cookie(
                name = it.name(),
                value = it.value(),
                maxAge = if (it.maxAge() == MIN_VALUE) -1 else it.maxAge(),
                secure = it.isSecure,
                path = it.path() ?: "/",
                httpOnly = it.isHttpOnly,
                sameSite = (it as? DefaultCookie)?.sameSite()?.let { ss ->
                    when (ss) {
                        Strict -> CookieSameSite.STRICT
                        Lax -> CookieSameSite.LAX
                        None -> CookieSameSite.NONE
                    }
                },
                domain = it.domain(),
            )
        }
    }

    override val body: Any by lazy {
        val content =
            if (req is ByteBufHolder) req.content()
            else Unpooled.buffer(0)

        if (content.isReadable) ByteBufUtil.getBytes(content)
        else byteArrayOf()
    }

    override val headers: Headers by lazy {
        Headers(
            nettyHeaders.names()
                .toList()
                .map { it.lowercase() }
                .flatMap { h -> nettyHeaders.getAll(h).map { Field(h, it) } }
        )
    }

    override val contentType: ContentType? by lazy {
        nettyHeaders[CONTENT_TYPE]?.let { parseContentType(it) }
    }

    override val authorization: Authorization? by lazy { authorization() }

    override fun with(
        body: Any,
        headers: Headers,
        contentType: ContentType?,
        method: HttpMethod,
        protocol: HttpProtocol,
        host: String,
        port: Int,
        path: String,
        queryParameters: Parameters,
        parts: List<HttpPart>,
        formParameters: Parameters,
        cookies: List<com.hexagontk.http.model.Cookie>,
        accept: List<ContentType>,
        authorization: Authorization?,
        certificateChain: List<X509Certificate>
    ): HttpRequestPort =
        throw UnsupportedOperationException()
}
