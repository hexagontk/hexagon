package com.hexagonkt.http.server.test

import com.hexagonkt.http.Cookie
import com.hexagonkt.http.Method
import com.hexagonkt.http.Part
import com.hexagonkt.http.server.RequestPort
import java.security.cert.X509Certificate

internal class MockRequest(private val testRequest: TestRequest) : RequestPort {

    override fun contentLength(): Long = testRequest.contentLength
    override fun contentType(): String? = testRequest.contentType
    override fun cookies(): Map<String, Cookie> = testRequest.cookies
    override fun formParameters(): Map<String, List<String>> = testRequest.formParameters
    override fun certificateChain(): List<X509Certificate> = testRequest.certificateChain

    override fun headers(): Map<String, List<String>> = testRequest.headers
    override fun host(): String = testRequest.host
    override fun ip(): String = testRequest.ip
    override fun loadBody(): String = testRequest.body
    override fun method(): Method = testRequest.method
    override fun parts(): Map<String, Part> = testRequest.parts
    override fun path(): String = testRequest.path
    override fun pathParameters(): Map<String, String> = testRequest.pathParameters
    override fun port(): Int = testRequest.port
    override fun queryParameters(): Map<String, List<String>> = testRequest.queryParameters
    override fun queryString(): String = testRequest.queryString
    override fun scheme(): String = testRequest.scheme
    override fun url(): String = testRequest.url
}
