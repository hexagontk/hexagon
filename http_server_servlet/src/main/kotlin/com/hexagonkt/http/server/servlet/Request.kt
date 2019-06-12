package com.hexagonkt.http.server.servlet

import com.hexagonkt.http.Method
import com.hexagonkt.http.server.Part
import com.hexagonkt.http.Path
import com.hexagonkt.http.parseQueryParameters
import com.hexagonkt.http.server.Request
import java.io.InputStreamReader
import java.net.HttpCookie
import javax.servlet.http.HttpServletRequest

internal class Request(private val req: HttpServletRequest) : Request() {
    var actionPath: Path? = null

    override fun path(): String = if (req.servletPath.isEmpty()) req.pathInfo else req.servletPath
    override fun body(): String = InputStreamReader(req.inputStream).readText()
    override fun scheme(): String = req.scheme
    override fun port(): Int = req.serverPort
    override fun method(): Method = Method.valueOf (req.method)
    override fun queryString(): String = req.queryString ?: ""
    override fun contentLength(): Long = req.contentLength.toLong()
    override fun contentType(): String? = req.contentType
    override fun host(): String = req.remoteHost
    override fun url(): String = req.requestURL.toString()
    override fun ip(): String = req.remoteAddr

    override fun pathParameters(): Map<String, String> =
        actionPath?.extractParameters(path()) ?: emptyMap()

    override fun parameters(): Map<String, List<String>> =
        req.parameterMap.map { it.key as String to it.value.toList() }.toMap()
    override fun queryParameters(): Map<String, List<String>> =
        parseQueryParameters(queryString)
    override fun formParameters(): Map<String, List<String>> =
        parameters.filter { it.key !in queryParameters.keys }

    override fun headers(): Map<String, List<String>> =
        req.headerNames.toList().map { it to req.getHeaders(it).toList() }.toMap()

    override fun cookies(): Map<String, HttpCookie> =
        try {
            val map = req.cookies.map { HttpCookie(it.name, it.value) }.map { it.name to it }
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
