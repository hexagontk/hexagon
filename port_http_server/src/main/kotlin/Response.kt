package com.hexagonkt.http.server

import java.io.OutputStream
import java.net.HttpCookie

/**
 * Check 'Request' comment. And also note that lists should be updated by engines after callback
 * is processed, this data is like a buffer that needs to be dumped to the real response.
 *
 * HTTP response context.
 */
abstract class Response () {
    val outputStream: OutputStream by lazy { outputStream() }

    var status: Int
        get() = status()
        set(value) { status(value) }

    var body: Any
        get() = body()
        set(value) { body(value) }

    var contentType: String?
        get() = contentType()
        set(value) { contentType(value) }

    val headers: MutableMap<String, List<String>> by lazy { LinkedHashMap<String, List<String>>() }

    fun setHeader(name: String, value: String) {
        headers[name] = listOf(value)
    }

    protected abstract fun outputStream(): OutputStream

    protected abstract fun status(): Int
    protected abstract fun status(value: Int)

    protected abstract fun body(): Any
    protected abstract fun body(value: Any)

    protected abstract fun contentType(): String?
    protected abstract fun contentType(value: String?)

    abstract fun setCookie (cookie: HttpCookie)
    abstract fun removeCookie (name: String)

    abstract fun redirect (url: String)
}
