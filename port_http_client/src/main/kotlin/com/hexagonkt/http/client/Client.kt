package com.hexagonkt.http.client

import com.hexagonkt.http.Method.*
import com.hexagonkt.http.Path
import com.hexagonkt.injection.InjectionManager
import java.net.HttpCookie

/**
 * Client to use other REST services.
 */
class Client(
    private val adapter: ClientPort = InjectionManager.inject(),
    val endpoint: String = "",
    val settings: ClientSettings = ClientSettings()
) {

    val cookies: MutableMap<String, HttpCookie> = mutableMapOf()

    constructor(endpoint: String = "", settings: ClientSettings = ClientSettings()) :
        this(InjectionManager.inject(), endpoint, settings)

    /**
     * Synchronous execution.
     */
    fun send(request: Request): Response =
        adapter.send(this, request)

    fun get(
        url: String,
        callHeaders: Map<String, List<String>> = emptyMap(),
        body: Any? = null,
        contentType: String? = settings.contentType): Response =
            send(Request(GET, Path(url), body, headers = callHeaders, contentType = contentType))

    fun head(url: String, callHeaders: Map<String, List<String>> = emptyMap()): Response =
            send(Request(HEAD, Path(url), null, headers = callHeaders))

    fun post(
        url: String, body: Any? = null, contentType: String? = settings.contentType): Response =
            send(Request(POST, Path(url), body, contentType = contentType))

    fun put(
        url: String,
        body: Any? = null,
        contentType: String? = settings.contentType): Response =
            send(Request(PUT, Path(url), body, contentType = contentType))

    fun delete(
        url: String,
        body: Any? = null,
        contentType: String? = settings.contentType): Response =
            send(Request(DELETE, Path(url), body, contentType = contentType))

    fun trace(
        url: String,
        body: Any? = null,
        contentType: String? = settings.contentType,
        callback: Response.() -> Unit = {}): Response =
            send(Request(TRACE, Path(url), body, contentType = contentType)).apply(callback)

    fun options(
        url: String,
        body: Any? = null,
        contentType: String? = settings.contentType,
        callHeaders: Map<String, List<String>> = emptyMap()): Response =
            send(Request(OPTIONS, Path(url), body, callHeaders, contentType = contentType))

    fun patch(
        url: String,
        body: Any? = null,
        contentType: String? = settings.contentType): Response =
            send(Request(PATCH, Path(url), body, contentType = contentType))
}
