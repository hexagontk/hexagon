package com.hexagonkt.http.server.vertx

import com.hexagonkt.http.model.*
import com.hexagonkt.http.parseContentType
import com.hexagonkt.http.parseQueryString
import io.vertx.core.http.HttpHeaders.CONTENT_TYPE
import io.vertx.core.http.HttpHeaders.CONTENT_LENGTH
import io.vertx.core.http.HttpServerRequest
import io.vertx.ext.web.RoutingContext
import java.io.File
import java.security.cert.X509Certificate

internal class VertxRequestAdapter(context: RoutingContext) : HttpRequestPort {

    private val req: HttpServerRequest = context.request()

    override val certificateChain: List<X509Certificate> by lazy {
        req.sslSession().peerCertificates.map { it as X509Certificate }
    }

    override val accept: List<ContentType> by lazy {
        req.headers().getAll("accept")?.map { parseContentType(it) } ?: emptyList()
    }

    override val contentLength: Long by lazy { req.getHeader(CONTENT_LENGTH).toLong() }

    override val queryParameters: QueryParameters by lazy {
        parseQueryString(req.query() ?: "")
    }

    override val method: HttpMethod by lazy {
        HttpMethod.valueOf(req.method().name())
    }

    override val protocol: HttpProtocol by lazy { HttpProtocol.valueOf(req.scheme().uppercase()) }
    override val host: String by lazy { req.remoteAddress().host() }
    override val port: Int by lazy { req.remoteAddress().port() }
    override val path: String by lazy { req.path() }
    override val authorization: Authorization? by lazy { authorization() }

    override val cookies: List<Cookie> by lazy {
        val vertxCookies = req.cookies()
        vertxCookies
            ?.map {
                Cookie(
                    it.name,
                    it.value,
                    if (it.maxAge < 0) -1 else it.maxAge,
                    it.isSecure,
//                    it.path,
                    httpOnly = it.isHttpOnly,
//                    domain = it.domain,
//                    sameSite = it.sameSite == CookieSameSite.STRICT,
                )
            }
            ?: emptyList()
    }

    override val headers: Headers by lazy {
        val vertxHeaders = req.headers()
        Headers(vertxHeaders.names().map { Header(it, vertxHeaders.getAll(it).toList()) })
    }

    override val contentType: ContentType? by lazy {
        req.getHeader("content-type")?.let { parseContentType(it) }
    }

    // TODO This is not optimal at all
    override val parts: List<HttpPart> by lazy {
        val formAttributes = req.formAttributes()
        val fields = formAttributes.names().map { HttpPart(it, formAttributes.get(it)) }

        val files = context.fileUploads().map {
            HttpPart(it.name(), it.uploadedFileName().let(::File).readBytes(), it.fileName())
        }

        files + fields
    }

    override val formParameters: FormParameters by lazy {
        if (req.getHeader(CONTENT_TYPE).contains("multipart/form-data")) {
            val formAttributes = req.formAttributes()
            val fields = formAttributes.names().map { FormParameter(it, formAttributes.getAll(it)) }

            FormParameters(fields)
        }
        else {
            FormParameters()
        }
    }

    override val body: Any by lazy {
        if (req.getHeader(CONTENT_LENGTH).toInt() == 0) ByteArray(0)
        else context.body().buffer().bytes
    }

    override fun with(
        body: Any,
        headers: Headers,
        contentType: ContentType?,
        method: HttpMethod,
        protocol: HttpProtocol,
        host: String,
        port: Int,
        path: String,
        queryParameters: QueryParameters,
        parts: List<HttpPart>,
        formParameters: FormParameters,
        cookies: List<Cookie>,
        accept: List<ContentType>,
        authorization: Authorization?,
        certificateChain: List<X509Certificate>
    ): HttpRequestPort =
        throw UnsupportedOperationException()
}
