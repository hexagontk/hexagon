package co.there4.hexagon.server.undertow

import co.there4.hexagon.server.HttpMethod
import co.there4.hexagon.server.Part
import io.undertow.server.HttpServerExchange

import co.there4.hexagon.server.EngineRequest
import co.there4.hexagon.server.Route
import java.net.HttpCookie

class UndertowRequest(val e: HttpServerExchange, val route: Route) : EngineRequest {
    override val path: String = e.requestPath
    override val contentType: String? get() = throw UnsupportedOperationException()
    override val body: String get() = throw UnsupportedOperationException()
    override val scheme: String get() = e.requestScheme
    override val port: Int get() = e.hostPort
    override val method: HttpMethod by lazy { HttpMethod.valueOf (e.requestMethod.toString()) }
    override val queryString: String get() = e.queryString
    override val contentLength: Long get() = e.requestContentLength
    override val host: String get() = e.hostName

    override val parameters: Map<String, List<String>>
        get() = (e.pathParameters + e.queryParameters).mapValues { it.value.toList() }

    override val headers: Map<String, List<String>> get() =
        e.requestHeaders.map { it.headerName.toString() to it }.toMap()

    override val userAgent: String get() = throw UnsupportedOperationException()
    override val url: String get() = e.requestURL
    override val ip: String get() = e.hostName
    override val cookies: MutableMap<String, HttpCookie> get() = throw UnsupportedOperationException()
    override val parts: Map<String, Part> get() = throw UnsupportedOperationException()
    override val referrer: String get() = throw UnsupportedOperationException()
    override val secure: Boolean get() = throw UnsupportedOperationException()
    override val forwarded: Boolean get() = throw UnsupportedOperationException()
    override val xhr: Boolean get() = throw UnsupportedOperationException()
    override val preferredType: String get() = throw UnsupportedOperationException()
}
