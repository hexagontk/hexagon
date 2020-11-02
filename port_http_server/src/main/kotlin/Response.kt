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

    /**
     * Output Stream of the response.
     */
    val outputStream: OutputStream by lazy { outputStream() }

    /**
     * Status code of the response.
     */
    var status: Int
        get() = status()
        set(value) { status(value) }

    /**
     * Body of the response.
     */
    var body: Any
        get() = body()
        set(value) { body(value) }

    /**
     * Content Type of the response.
     */
    var contentType: String?
        get() = contentType()
        set(value) { contentType(value) }

    /**
     * Response headers.
     */
    @Suppress("RemoveExplicitTypeArguments") // Without types fails inside IntelliJ (not in CLI)
    val headersValues: MutableMap<String, List<Any>> by lazy { LinkedHashMap<String, List<Any>>() }

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
     * Provides a [OutputStream] instance for the response.
     */
    protected abstract fun outputStream(): OutputStream

    /**
     * Provides the status code of the response.
     */
    protected abstract fun status(): Int

    /**
     * Sets the status code of the response.
     *
     * @param value Status code.
     */
    protected abstract fun status(value: Int)

    /**
     * Provides the body of the response.
     */
    protected abstract fun body(): Any

    /**
     * Sets the body of the response.
     *
     * @param value Body content.
     */
    protected abstract fun body(value: Any)

    /**
     * Provides the content type of the response.
     */
    protected abstract fun contentType(): String?

    /**
     * Sets the content type of the response.
     *
     * @param value Content type info.
     */
    protected abstract fun contentType(value: String?)

    /**
     * Sends a response by adding the given cookie.
     *
     * @param cookie Cookie to be added.
     */
    abstract fun addCookie (cookie: HttpCookie)

    /**
     * Sends a response by removing the cookie with specified name.
     *
     * @param name Cookie to be deleted.
     */
    abstract fun removeCookie (name: String)

    /**
     * Sends a redirect response to the client using the
     * specified redirect URL.
     *
     * @param url Redirect URL.
     */
    abstract fun redirect (url: String)
}
