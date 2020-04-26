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

    interface MapInterface<K, V> {
        operator fun get(name: K): V?

        operator fun set(name: K, value: V?)

        fun remove(name: K)
    }

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

    @Suppress("RemoveExplicitTypeArguments") // Without types fails inside IntelliJ (not in CLI)
    val headersValues: MutableMap<String, List<Any>> by lazy { LinkedHashMap<String, List<Any>>() }

    val headers: MapInterface<String, Any> = object : MapInterface<String, Any> {
        override operator fun get(name: String): Any? =
            headersValues[name]?.firstOrNull()

        override operator fun set(name: String, value: Any?) {
            if (value == null)
                remove(name)
            else
                headersValues[name] = listOf(value)
        }

        override fun remove(name: String) {
            headersValues.remove(name)
        }
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
