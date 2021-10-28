package com.hexagonkt.http.server.test

import com.hexagonkt.http.Cookie
import com.hexagonkt.http.Method
import com.hexagonkt.http.Part
import com.hexagonkt.http.server.RequestPort
import java.security.cert.X509Certificate

data class TestRequest(
    val method: Method = Method.GET,
    val scheme: String = "http",
    val host: String = "localhost",
    val ip: String = "127.0.0.1",
    val port: Int = 9090,
    val path: String = "/",
    val pathParameters: Map<String, String> = emptyMap(),
    val queryString: String = "",
    val url: String = "$scheme://$host$path",
    val parts: Map<String, Part> = emptyMap(),
    val queryParameters: Map<String, List<String>> = emptyMap(),
    val formParameters: Map<String, List<String>> = emptyMap(),
    val certificateChain: List<X509Certificate> = emptyList(),
    val body: String = "",
    val headers: Map<String, List<String>> = emptyMap(),
    val cookies: Map<String, Cookie> = emptyMap(),
    val contentType: String? = null,
    val contentLength: Long = body.length.toLong()
) : RequestPort {

    override fun contentLength(): Long = contentLength
    override fun contentType(): String? = contentType
    override fun cookies(): Map<String, Cookie> = cookies
    override fun formParameters(): Map<String, List<String>> = formParameters
    override fun certificateChain(): List<X509Certificate> = certificateChain

    override fun headers(): Map<String, List<String>> = headers
    override fun host(): String = host
    override fun ip(): String = ip
    override fun loadBody(): String = body
    override fun method(): Method = method
    override fun parts(): Map<String, Part> = parts
    override fun path(): String = path
    override fun pathParameters(): Map<String, String> = pathParameters
    override fun port(): Int = port
    override fun queryParameters(): Map<String, List<String>> = queryParameters
    override fun queryString(): String = queryString
    override fun scheme(): String = scheme
    override fun url(): String = url
}
