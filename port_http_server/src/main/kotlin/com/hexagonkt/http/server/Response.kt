package com.hexagonkt.http.server

import java.io.OutputStream
import java.net.HttpCookie

/**
 * Check 'Request' comment. And also note that lists should be updated by engines after callback
 * is processed, this data is like a buffer that needs to be dumped to the real response.
 *
 * HTTP response context.
 */
abstract class Response {
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

    @Suppress("RemoveExplicitTypeArguments") // Without types fails inside IntelliJ
    val headers: MutableMap<String, List<Any>> by lazy { LinkedHashMap<String, List<Any>>() }

    fun setHeader(name: String, value: Any?) {
        if (value != null)
            headers[name] = listOf(value)
    }

    protected abstract fun outputStream(): OutputStream

    protected abstract fun status(): Int
    protected abstract fun status(value: Int)

    protected abstract fun body(): Any
    protected abstract fun body(value: Any)

    protected abstract fun contentType(): String?
    protected abstract fun contentType(value: String?)

    abstract fun addCookie (cookie: HttpCookie)
    abstract fun removeCookie (name: String)

    abstract fun redirect (url: String)
}
