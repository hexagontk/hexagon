package co.there4.hexagon.web.servlet

import co.there4.hexagon.util.parseQueryParameters
import co.there4.hexagon.web.HttpMethod
import co.there4.hexagon.web.Part
import co.there4.hexagon.web.Path
import co.there4.hexagon.web.Request
import java.io.InputStreamReader
import java.net.HttpCookie
import javax.servlet.http.HttpServletRequest

internal class BServletRequest(val req: HttpServletRequest, var actionPath: Path? = null) :
    Request {

    override val scriptName: String = req.pathInfo ?: req.servletPath
    override val pathInfo: String = scriptName
    override val path: String = scriptName
    override val body: String by lazy { InputStreamReader(req.inputStream).readText() }
    override val scheme: String by lazy { req.scheme }
    override val port: Int by lazy { req.serverPort }
    override val method: HttpMethod by lazy { HttpMethod.valueOf (req.method) }
    override val queryString: String by lazy { req.queryString }
    override val contentLength: Long by lazy { req.contentLength.toLong() }
    override val contentType: String? by lazy { req.contentType }
    override val host: String by lazy { req.remoteHost }
    override val userAgent: String by lazy { headers["User-Agent"]?.first() ?: "UNKNOWN" }
    override val url: String by lazy { req.requestURL.toString() }
    override val ip: String by lazy { req.remoteAddr }
    override val referrer: String by lazy { throw UnsupportedOperationException ()  }
    override val secure: Boolean by lazy { throw UnsupportedOperationException ()  }
    override val forwarded: Boolean by lazy { throw UnsupportedOperationException ()  }
    override val xhr: Boolean by lazy { throw UnsupportedOperationException ()  }
    override val preferredType: String by lazy { throw UnsupportedOperationException ()  }

    override val parameters: Map<String, List<String>> by lazy {
        (actionPath?.extractParameters(req.servletPath)?:mapOf()).mapValues { listOf(it.value) } +
        req.parameterMap.map { it.key as String to it.value.toList() }.toMap() +
        parseQueryParameters(req.queryString ?: "").mapValues { listOf(it.value) }
    }

    override val headers: Map<String, List<String>> by lazy {
        req.headerNames.toList().map { it to req.getHeaders(it).toList() }.toMap()
    }

    override val cookies: Map<String, HttpCookie> get() {
        try {
            val map = req.cookies.map { HttpCookie(it.name, it.value) }.map { it.name to it }
            return map.toMap()
        }
        catch (e: Exception) {
            return mapOf()
        }
    }

    override val parts: Map<String, Part> by lazy { throw UnsupportedOperationException ()  }
}
