package com.hexagontk.http.server.jdk

import com.hexagontk.http.model.*
import com.hexagontk.http.model.HttpProtocol.HTTP
import com.hexagontk.http.model.HttpProtocol.HTTPS
import com.hexagontk.http.parseContentType
import com.hexagontk.http.parseQueryString
import com.hexagontk.http.patterns.PathPattern
import com.sun.net.httpserver.HttpExchange
import com.sun.net.httpserver.HttpsExchange
import java.security.cert.X509Certificate
import javax.net.ssl.SSLSession

class JdkRequestAdapter(
    methodName: String,
    exchange: HttpExchange,
) : HttpRequestPort {

    private val sslSession: SSLSession by lazy {
        val x = exchange as? HttpsExchange ?: error("SSL session cannot be access in HTTP request")
        x.sslSession
    }

    override val certificateChain: List<X509Certificate> by lazy {
        sslSession.localCertificates.map { it as X509Certificate }
    }

    override val accept: List<ContentType> by lazy {
        headers.all["accept"]?.map { parseContentType(it.text) } ?: emptyList()
    }

    override val contentLength: Long by lazy {
        (body as ByteArray).size.toLong()
    }

    override val queryParameters: Parameters by lazy {
        exchange.requestURI.query?.let {parseQueryString(it) } ?: Parameters()
    }

    override val parts: List<HttpPart> by lazy {
        emptyList()
    }

    override val formParameters: Parameters by lazy {
        Parameters()
    }

    override val method: HttpMethod by lazy {
        HttpMethod.valueOf(methodName)
    }

    override val protocol: HttpProtocol by lazy {
        if (exchange.protocol.startsWith("HTTPS")) HTTPS
        else HTTP
    }

    override val host: String by lazy {
        exchange.remoteAddress.hostName
    }

    override val port: Int by lazy {
        exchange.requestURI.port
    }

    override val path: String by lazy {
        exchange.requestURI.path
    }

    override val cookies: List<Cookie> by lazy {
        emptyList()
    }

    override val body: Any by lazy {
        exchange.requestBody.readAllBytes()
    }

    override val headers: Headers by lazy {
        Headers(exchange.requestHeaders.flatMap { (k, v) -> v.map { Header(k, it) }})
    }

    override val contentType: ContentType? by lazy {
        headers["content-type"]?.let { parseContentType(it.text) }
    }

    override val authorization: Authorization? by lazy { authorization() }

    override val pathPattern: PathPattern? = null

    override val pathParameters: Map<String, Any> = emptyMap()

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
        cookies: List<Cookie>,
        accept: List<ContentType>,
        authorization: Authorization?,
        certificateChain: List<X509Certificate>,
        pathPattern: PathPattern?,
        pathParameters: Map<String, Any>,
    ): HttpRequestPort =
        throw UnsupportedOperationException()
}
