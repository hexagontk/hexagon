package com.hexagonkt.http.server

import com.hexagonkt.http.Cookie
import java.io.OutputStream

interface ResponsePort {

    /**
     * Provides a [OutputStream] instance for the response.
     */
    fun outputStream(): OutputStream

    /**
     * Provides the status code of the response.
     */
    fun status(): Int

    /**
     * Sets the status code of the response.
     *
     * @param value Status code.
     */
    fun status(value: Int)

    /**
     * Provides the body of the response.
     */
    fun body(): Any

    /**
     * Sets the body of the response.
     *
     * @param value Body content.
     */
    fun body(value: Any)

    /**
     * Provides the content type of the response.
     */
    fun contentType(): String?

    /**
     * Sets the content type of the response.
     *
     * @param value Content type info.
     */
    fun contentType(value: String?)

    /**
     * Sends a response by adding the given cookie.
     *
     * @param cookie Cookie to be added.
     */
    fun addCookie (cookie: Cookie)

    /**
     * Sends a response by removing the cookie with specified name.
     *
     * @param name Cookie to be deleted.
     */
    fun removeCookie (name: String)

    /**
     * Sends a redirect response to the client using the
     * specified redirect URL.
     *
     * @param url Redirect URL.
     */
    fun redirect (url: String)
}
