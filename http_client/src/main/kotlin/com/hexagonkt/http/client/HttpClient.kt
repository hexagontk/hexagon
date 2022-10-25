package com.hexagonkt.http.client

import com.hexagonkt.http.client.model.HttpClientRequest
import com.hexagonkt.http.client.model.HttpClientResponse
import com.hexagonkt.http.model.*
import com.hexagonkt.http.model.HttpMethod.*
import java.io.Closeable
import java.net.URL

/**
 * Client to use other REST services.
 *
 * TODO Add support for client filters. I.e.: for auth, signing, etc.
 */
class HttpClient(
    private val adapter: HttpClientPort,
    val settings: HttpClientSettings = HttpClientSettings()
) : Closeable {

    constructor(
        adapter: HttpClientPort,
        baseUrl: URL = URL("http://localhost:8080"),
        settings: HttpClientSettings = HttpClientSettings()
    ) :
        this(adapter, settings.copy(baseUrl = baseUrl))

    var cookies: List<HttpCookie> = emptyList()

    override fun close() {
        stop()
    }

    fun cookiesMap(): Map<String, HttpCookie> =
        cookies.associateBy { it.name }

    fun start() {
        adapter.startUp(this)
    }

    fun stop() {
        adapter.shutDown()
    }

    /**
     * Synchronous execution.
     */
    fun send(request: HttpClientRequest): HttpClientResponse =
        adapter.send(request)

    fun get(
        path: String,
        headers: Headers = Headers(),
        body: Any? = null,
        contentType: ContentType? = settings.contentType): HttpClientResponse =
            send(
                HttpClientRequest(
                    method = GET,
                    path = path,
                    body = body ?: "",
                    headers = headers,
                    contentType = contentType)
            )

    fun head(
        path: String, headers: Headers = Headers()
    ): HttpClientResponse =
        send(HttpClientRequest(HEAD, path = path, body = ByteArray(0), headers = headers))

    fun post(
        path: String,
        body: Any? = null,
        contentType: ContentType? = settings.contentType
    ): HttpClientResponse =
        send(HttpClientRequest(POST, path = path, body = body ?: "", contentType = contentType))

    fun put(
        path: String,
        body: Any? = null,
        contentType: ContentType? = settings.contentType
    ): HttpClientResponse =
        send(HttpClientRequest(PUT, path = path, body = body ?: "", contentType = contentType))

    fun delete(
        path: String,
        body: Any? = null,
        contentType: ContentType? = settings.contentType
    ): HttpClientResponse =
        send(HttpClientRequest(DELETE, path = path, body = body ?: "", contentType = contentType))

    fun trace(
        path: String,
        body: Any? = null,
        contentType: ContentType? = settings.contentType
    ): HttpClientResponse =
        send(HttpClientRequest(TRACE, path = path, body = body ?: "", contentType = contentType))

    fun options(
        path: String,
        body: Any? = null,
        headers: Headers = Headers(),
        contentType: ContentType? = settings.contentType
    ): HttpClientResponse =
        send(
            HttpClientRequest(
                method = OPTIONS,
                path = path,
                body = body ?: "",
                headers = headers,
                contentType = contentType
            )
        )

    fun patch(
        path: String,
        body: Any? = null,
        contentType: ContentType? = settings.contentType
    ): HttpClientResponse =
        send(HttpClientRequest(PATCH, path = path, body = body ?: "", contentType = contentType))
}
