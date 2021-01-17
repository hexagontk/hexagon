package com.hexagonkt.http.server.servlet

import com.hexagonkt.http.Cookie
import com.hexagonkt.http.Method
import com.hexagonkt.http.Part
import com.hexagonkt.http.Path
import com.hexagonkt.http.parseQueryParameters
import com.hexagonkt.http.server.RequestPort
import java.io.InputStreamReader
import java.security.cert.X509Certificate
import javax.servlet.http.HttpServletRequest

internal class RequestAdapter(private val req: HttpServletRequest) : RequestPort {

    private val certificateAttribute = "javax.servlet.request.X509Certificate"

    private val parameters: Map<String, List<String>> by lazy {
        req.parameterMap.map { it.key as String to it.value.toList() }.toMap()
    }

    var actionPath: Path? = null

    override fun path(): String = if (req.servletPath.isEmpty()) req.pathInfo else req.servletPath
    override fun loadBody(): String = InputStreamReader(req.inputStream).readText()
    override fun scheme(): String = req.scheme
    override fun port(): Int = req.serverPort
    override fun method(): Method = Method.valueOf(req.method)
    override fun queryString(): String = req.queryString ?: ""
    override fun contentLength(): Long = req.contentLength.toLong()
    override fun contentType(): String? = req.contentType
    override fun host(): String = req.remoteHost
    override fun url(): String = req.requestURL.toString()
    override fun ip(): String = req.remoteAddr

    override fun pathParameters(): Map<String, String> =
        actionPath?.extractParameters(path()) ?: emptyMap()

    override fun queryParameters(): Map<String, List<String>> =
        parseQueryParameters(queryString())

    override fun formParameters(): Map<String, List<String>> =
        parameters.filter { it.key !in queryParameters().keys }

    @Suppress("UNCHECKED_CAST")
    override fun certificateChain(): List<X509Certificate> =
        (req.getAttribute(certificateAttribute) as? Array<X509Certificate>)
            ?.toList()
            ?: emptyList()

    override fun headers(): Map<String, List<String>> =
        req.headerNames.toList().map { it to req.getHeaders(it).toList() }.toMap()

    override fun cookies(): Map<String, Cookie> =
        try {
            val map = req.cookies.map { Cookie(it.name, it.value) }.map { it.name to it }
            map.toMap()
        }
        catch (e: Exception) {
            mapOf()
        }

    override fun parts(): Map<String, Part> =
        req.parts
            .map {
                Part(
                    contentType = it.contentType,
                    headers = it.headerNames
                        .filterNotNull()
                        .map { hn -> hn to it.getHeaders(hn).toList() }
                        .toMap(),
                    inputStream = it.inputStream,
                    name = it.name,
                    size = it.size,
                    submittedFileName = it.submittedFileName
                )
            }
            .associateBy { it.name }
}
