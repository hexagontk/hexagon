package com.hexagonkt.http.server.netty

import com.hexagonkt.core.MultiMap
import com.hexagonkt.http.model.*
import com.hexagonkt.http.parseContentType
import com.hexagonkt.http.parseQueryString
import com.hexagonkt.http.server.model.HttpServerRequestPort
import io.netty.handler.codec.http.FullHttpRequest
import java.net.URI
import java.security.cert.X509Certificate
import io.netty.handler.codec.http.HttpMethod as NettyHttpMethod

class NettyRequestAdapter(
    methodName: NettyHttpMethod,
    req: FullHttpRequest,
) : HttpServerRequestPort {

    private val uri by lazy { URI(req.uri()) }

    @Suppress("UNCHECKED_CAST")
    override val certificateChain: List<X509Certificate> by lazy {
        TODO()
    }

    override val accept: List<ContentType> by lazy {
        req.headers().getAll("accept").map { parseContentType(it) }
    }

    override val contentLength: Long by lazy {
        req.headers()["content-length"]?.toLong() ?: 0L
    }

    override val queryParameters: MultiMap<String, String> by lazy {
        parseQueryString(uri.query)
    }
    override val parts: List<HttpPartPort>
        get() = TODO("Not yet implemented")
    override val formParameters: MultiMap<String, String>
        get() = TODO("Not yet implemented")

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

    override val body: Any
        get() = TODO("Not yet implemented")

    override val headers: MultiMap<String, String> by lazy {
//        val headers1: HttpHeaders = req.headers()
//        MultiMap(
//            req.headerNames
//                .toList()
//                .map { it.lowercase() }
//                .associateWith { req.getHeaders(it).toList() }
//        )
        TODO()
    }

    override val contentType: ContentType? by lazy {
        req.headers()["accept"]?.let { parseContentType(it) }
    }
}
