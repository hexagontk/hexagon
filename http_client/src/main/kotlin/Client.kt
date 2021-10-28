package com.hexagonkt.http.client

import com.hexagonkt.http.Cookie
import com.hexagonkt.http.Method.*
import com.hexagonkt.http.Path
import com.hexagonkt.serialization.SerializationFormat

/**
 * Client to use other REST services.
 */
class Client(
    private val adapter: ClientPort,
    val endpoint: String = "",
    val settings: ClientSettings = ClientSettings()
) {

    val cookies: MutableMap<String, Cookie> = mutableMapOf()

    /**
     * Synchronous execution.
     */
    fun send(request: Request): Response =
        adapter.send(this, request)

    fun get(
        path: String,
        headers: Map<String, List<String>> = emptyMap(),
        body: Any? = null,
        contentType: String? = settings.contentType): Response =
            send(Request(GET, Path(path), body, headers = headers, contentType = contentType))

    fun head(path: String, headers: Map<String, List<String>> = emptyMap()): Response =
            send(Request(HEAD, Path(path), null, headers = headers))

    fun post(
        path: String, body: Any? = null, contentType: String? = settings.contentType): Response =
            send(Request(POST, Path(path), body, contentType = contentType))

    fun put(
        path: String,
        body: Any? = null,
        contentType: String? = settings.contentType): Response =
            send(Request(PUT, Path(path), body, contentType = contentType))

    fun delete(
        path: String,
        body: Any? = null,
        contentType: String? = settings.contentType): Response =
            send(Request(DELETE, Path(path), body, contentType = contentType))

    fun trace(
        path: String,
        body: Any? = null,
        contentType: String? = settings.contentType): Response =
            send(Request(TRACE, Path(path), body, contentType = contentType))

    fun options(
        path: String,
        body: Any? = null,
        contentType: String? = settings.contentType,
        headers: Map<String, List<String>> = emptyMap()): Response =
            send(Request(OPTIONS, Path(path), body, headers, contentType = contentType))

    fun patch(
        path: String,
        body: Any? = null,
        contentType: String? = settings.contentType): Response =
            send(Request(PATCH, Path(path), body, contentType = contentType))

    fun get(
        path: String,
        headers: Map<String, List<String>> = emptyMap(),
        body: Any,
        format: SerializationFormat): Response =
            get(path, headers, body, format.contentType)

    fun post(path: String, body: Any, format: SerializationFormat): Response =
        post(path, body, format.contentType)

    fun put(
        path: String,
        body: Any,
        format: SerializationFormat): Response =
            put(path, body, format.contentType)

    fun delete(
        path: String,
        body: Any,
        format: SerializationFormat): Response =
            delete(path, body, format.contentType)

    fun trace(
        path: String,
        body: Any,
        format: SerializationFormat): Response =
            trace(path, body, format.contentType)

    fun options(
        path: String,
        body: Any,
        format: SerializationFormat,
        headers: Map<String, List<String>> = emptyMap()): Response =
            options(path, body, format.contentType, headers)

    fun patch(
        path: String,
        body: Any,
        format: SerializationFormat): Response =
            patch(path, body, format.contentType)
}
