package com.hexagonkt.server.servlet

import com.hexagonkt.helpers.parseQueryParameters
import com.hexagonkt.server.HttpMethod
import com.hexagonkt.server.Part
import com.hexagonkt.server.Path
import com.hexagonkt.server.EngineRequest
import java.io.InputStreamReader
import java.net.HttpCookie
import javax.servlet.http.HttpServletRequest

internal class BServletRequest(private val req: HttpServletRequest) : EngineRequest {
    var actionPath: Path? = null

    override val path: String by lazy {
        if (req.servletPath.isEmpty()) req.pathInfo else req.servletPath
    }
    override val body: String by lazy { InputStreamReader(req.inputStream).readText() }
    override val scheme: String by lazy { req.scheme }
    override val port: Int by lazy { req.serverPort }
    override val method: HttpMethod by lazy { HttpMethod.valueOf (req.method) }
    override val queryString: String by lazy { req.queryString }
    override val contentLength: Long by lazy { req.contentLength.toLong() }
    override val contentType: String? by lazy { req.contentType }
    override val host: String by lazy { req.remoteHost }
    override val url: String by lazy { req.requestURL.toString() }
    override val ip: String by lazy { req.remoteAddr }

    override val userAgent: String by lazy { headers["User-Agent"]?.first() ?: "UNKNOWN" }
    override val referer: String by lazy { headers["Referer"]?.first() ?: "UNKNOWN" }
    override val secure: Boolean by lazy { scheme == "https" }

    override val parameters: Map<String, List<String>> by lazy {
        val requestUrl = if (req.servletPath.isEmpty()) req.pathInfo else req.servletPath
        (actionPath?.extractParameters(requestUrl)?:mapOf()).mapValues { listOf(it.value) } +
        req.parameterMap.map { it.key as String to it.value.toList() }.toMap() +
        parseQueryParameters(req.queryString ?: "").mapValues { listOf(it.value) }
    }

    override val headers: Map<String, List<String>> by lazy {
        req.headerNames.toList().map { it to req.getHeaders(it).toList() }.toMap()
    }

    override val cookies: Map<String, HttpCookie> get() =
        try {
            val map = req.cookies.map { HttpCookie(it.name, it.value) }.map { it.name to it }
            map.toMap()
        }
        catch (e: Exception) {
            mapOf()
        }

    override val parts: Map<String, Part> by lazy { throw UnsupportedOperationException ()  }
}
