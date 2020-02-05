package com.hexagonkt.http.server.test

import com.hexagonkt.http.Method
import com.hexagonkt.http.Part
import java.net.HttpCookie
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
    val parameters: Map<String, List<String>> = emptyMap(),
    val queryParameters: Map<String, List<String>> = emptyMap(),
    val formParameters: Map<String, List<String>> = emptyMap(),
    val certificateChain: List<X509Certificate> = emptyList(),
    val body: String = "",
    val headers: Map<String, List<String>> = emptyMap(),
    val cookies: Map<String, HttpCookie> = emptyMap(),
    val contentType: String? = null,
    val contentLength: Long = body.length.toLong()
)
