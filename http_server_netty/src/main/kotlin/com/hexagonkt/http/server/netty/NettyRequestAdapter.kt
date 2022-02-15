package com.hexagonkt.http.server.netty

import com.hexagonkt.core.MultiMap
import com.hexagonkt.http.model.*
import com.hexagonkt.http.parseContentType
import com.hexagonkt.http.server.model.HttpServerRequestPort
import io.netty.buffer.ByteBufUtil
import io.netty.handler.codec.http.FullHttpRequest
import io.netty.handler.codec.http.HttpHeaderNames.*
import io.netty.handler.codec.http.QueryStringDecoder
import java.net.URI
import java.security.cert.X509Certificate

class NettyRequestAdapter(
    methodName: io.netty.handler.codec.http.HttpMethod,
    req: FullHttpRequest,
    override val certificateChain: List<X509Certificate>,
) : HttpServerRequestPort {

    private val uri by lazy { URI(req.uri()) }

    override val accept: List<ContentType> by lazy {
        req.headers().getAll(ACCEPT).map { parseContentType(it) }
    }

    override val contentLength: Long by lazy {
        req.headers()[CONTENT_LENGTH]?.toLong() ?: 0L
    }

    override val queryParameters: MultiMap<String, String> by lazy {
        val queryStringDecoder = QueryStringDecoder(req.uri())
        MultiMap(queryStringDecoder.parameters())
    }

    override val parts: List<HttpPartPort> by lazy {
        TODO("Not yet implemented")
    }

    override val formParameters: MultiMap<String, String> by lazy {
        TODO("Not yet implemented")
    }

    override val method: HttpMethod by lazy {
        HttpMethod.valueOf(methodName.name())
    }

    override val protocol: HttpProtocol by lazy { HttpProtocol.valueOf(uri.scheme.uppercase()) }
    override val host: String by lazy { uri.host }
    override val port: Int by lazy { uri.port }
    override val path: String by lazy { uri.path }

    override val cookies: List<HttpCookie> by lazy {
//        req.cookies
//            ?.map { HttpCookie(it.name, it.value, it.maxAge.toLong(), it.secure) }
//            ?: emptyList()
        TODO()
    }

    override val body: Any by lazy {
        val content = req.content()

        if (content.isReadable)
            ByteBufUtil.getBytes(content)
        else
            error("Body content is not readable")
    }

    override val headers: MultiMap<String, String> by lazy {
        MultiMap(
            req.headers().names()
                .toList()
                .map { it.lowercase() }
                .associateWith { req.headers().getAll(it) }
        )
    }

    override val contentType: ContentType? by lazy {
        req.headers()[CONTENT_TYPE]?.let { parseContentType(it) }
    }
}
