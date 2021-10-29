package com.hexagonkt.http.server

import com.hexagonkt.http.Cookie
import java.io.OutputStream

/**
 * Check 'Request' comment. And also note that lists should be updated by engines after callback
 * is processed, this data is like a buffer that needs to be dumped to the real response.
 *
 * HTTP response context.
 */
class Response(val adapter: ResponsePort) {

    interface MapInterface<K, V> {
        operator fun get(name: K): V?

        operator fun set(name: K, value: V?)

        fun remove(name: K)
    }

    /**
     * Output Stream of the response.
     */
    val outputStream: OutputStream by lazy { adapter.outputStream() }

    /**
     * Status code of the response.
     */
    var status: Int
        get() = adapter.status()
        set(value) { adapter.status(value) }

    /**
     * Body of the response.
     */
    var body: Any
        get() = adapter.body()
        set(value) { adapter.body(value) }

    /**
     * Content Type of the response.
     */
    var contentType: String?
        get() = adapter.contentType()
        set(value) { adapter.contentType(value) }

    /**
     * Response headers.
     */
    val headersValues: MutableMap<String, List<Any>> by lazy { LinkedHashMap() }

    /**
     * A [MapInterface] implementation for response headers.
     */
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

    /**
     * Sends a redirect response to the client using the
     * specified redirect URL.
     *
     * @param url Redirect URL.
     */
    fun redirect (url: String) {
        adapter.redirect(url)
    }

    /**
     * Sends a response by adding the given cookie.
     *
     * @param cookie Cookie to be added.
     */
    fun addCookie (cookie: Cookie) {
        adapter.addCookie(cookie)
    }

    /**
     * Sends a response by removing the cookie with specified name.
     *
     * @param name Cookie to be deleted.
     */
    fun removeCookie (name: String) {
        adapter.removeCookie(name)
    }
}
