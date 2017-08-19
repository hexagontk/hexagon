package com.hexagonkt.server.undertow

import com.hexagonkt.server.HttpMethod
import com.hexagonkt.server.Part
import io.undertow.server.HttpServerExchange

import com.hexagonkt.server.EngineRequest
import java.net.HttpCookie

class UndertowRequest(private val e: HttpServerExchange) : EngineRequest {
    override val path: String = e.requestPath
    override val body: String get() = e.inputStream.reader().readText()
    override val scheme: String get() = e.requestScheme
    override val port: Int get() = e.hostPort
    override val method: HttpMethod by lazy { HttpMethod.valueOf (e.requestMethod.toString()) }
    override val queryString: String get() = e.queryString
    override val contentLength: Long get() = e.requestContentLength
    override val contentType: String? get() = e.requestHeaders.getFirst("content-type")
    override val host: String get() = e.hostName
    override val url: String get() = e.requestURL
    override val ip: String get() = e.hostName

    override val userAgent: String by lazy { e.requestHeaders.getFirst("user-agent") ?: "UNKNOWN" }
    override val referer: String by lazy { headers["referer"]?.first() ?: "UNKNOWN" }
    override val secure: Boolean by lazy { scheme == "https" }

    override val parameters: Map<String, List<String>>
        get() = (e.pathParameters + e.queryParameters).mapValues { it.value.toList() }

    override val headers: Map<String, List<String>> get() =
        e.requestHeaders.map { it.headerName.toString() to it }.toMap()

    override val cookies: Map<String, HttpCookie> get() = e.requestCookies
        .map {
            val c = it.value
            val k = it.key
            k to HttpCookie(c.name, c.value)
        }
        .toMap()

    override val parts: Map<String, Part> get() = throw UnsupportedOperationException()
}
