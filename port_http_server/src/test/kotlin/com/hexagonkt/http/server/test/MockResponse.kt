package com.hexagonkt.http.server.test

import com.hexagonkt.http.server.Response
import java.io.OutputStream
import java.net.HttpCookie

internal class MockResponse(private val testResponse: TestResponse): Response() {
    override fun addCookie(cookie: HttpCookie) {
        testResponse.cookies = testResponse.cookies + (cookie.name to cookie)
    }

    override fun body(): Any = testResponse.body

    override fun body(value: Any) {
        testResponse.body = value
    }

    override fun contentType(): String? = testResponse.contentType

    override fun contentType(value: String?) {
        testResponse.contentType = value
    }

    override fun outputStream(): OutputStream = testResponse.outputStream

    override fun redirect(url: String) {
        testResponse.status = 301
        headers["location"] = listOf(url)
    }

    override fun removeCookie(name: String) {
        testResponse.cookies = testResponse.cookies - name
    }

    override fun status(): Int = testResponse.status

    override fun status(value: Int) {
        testResponse.status = value
    }
}
