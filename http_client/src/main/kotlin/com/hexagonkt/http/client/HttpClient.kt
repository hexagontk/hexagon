package com.hexagonkt.http.client

import com.hexagonkt.http.client.model.HttpClientRequest
import com.hexagonkt.http.client.model.HttpClientResponse
import com.hexagonkt.http.model.*
import com.hexagonkt.http.model.HttpMethod.*
import com.hexagonkt.http.model.ws.WsCloseStatus
import com.hexagonkt.http.model.ws.WsSession
import java.io.Closeable
import java.net.URL
import java.util.concurrent.Flow.Publisher

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
        baseUrl: URL,
        settings: HttpClientSettings = HttpClientSettings()
    ) :
        this(adapter, settings.copy(baseUrl = baseUrl))

    constructor(
        adapter: HttpClientPort,
        baseUrl: String,
        settings: HttpClientSettings = HttpClientSettings()
    ) :
        this(adapter, URL(baseUrl), settings)

    var cookies: List<Cookie> = emptyList()

    override fun close() {
        stop()
    }

    fun cookiesMap(): Map<String, Cookie> =
        cookies.associateBy { it.name }

    fun start() {
        adapter.startUp(this)
    }

    fun stop() {
        adapter.shutDown()
    }

    fun started(): Boolean =
        adapter.started()

    /**
     * Synchronous execution.
     */
    fun send(request: HttpClientRequest): HttpClientResponse =
        adapter.send(request)

    fun sse(request: HttpClientRequest): Publisher<ServerEvent> =
        adapter.sse(request)

    fun sse(path: String): Publisher<ServerEvent> =
        sse(HttpClientRequest(path = path))

    fun ws(
        path: String,
        onConnect: WsSession.() -> Unit = {},
        onBinary: WsSession.(data: ByteArray) -> Unit = {},
        onText: WsSession.(text: String) -> Unit = {},
        onPing: WsSession.(data: ByteArray) -> Unit = {},
        onPong: WsSession.(data: ByteArray) -> Unit = {},
        onClose: WsSession.(status: WsCloseStatus, reason: String) -> Unit = { _, _ -> },
    ): WsSession =
        adapter.ws(path, onConnect, onBinary, onText, onPing, onPong, onClose)

    // TODO Test without passing a path (request to baseUrl directly)
    fun get(
        path: String = "",
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

    fun head(path: String = "", headers: Headers = Headers()): HttpClientResponse =
        send(HttpClientRequest(HEAD, path = path, body = ByteArray(0), headers = headers))

    fun post(
        path: String = "",
        body: Any? = null,
        contentType: ContentType? = settings.contentType
    ): HttpClientResponse =
        send(HttpClientRequest(POST, path = path, body = body ?: "", contentType = contentType))

    fun put(
        path: String = "",
        body: Any? = null,
        contentType: ContentType? = settings.contentType
    ): HttpClientResponse =
        send(HttpClientRequest(PUT, path = path, body = body ?: "", contentType = contentType))

    fun delete(
        path: String = "",
        body: Any? = null,
        contentType: ContentType? = settings.contentType
    ): HttpClientResponse =
        send(HttpClientRequest(DELETE, path = path, body = body ?: "", contentType = contentType))

    fun trace(
        path: String = "",
        body: Any? = null,
        contentType: ContentType? = settings.contentType
    ): HttpClientResponse =
        send(HttpClientRequest(TRACE, path = path, body = body ?: "", contentType = contentType))

    fun options(
        path: String = "",
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
        path: String = "",
        body: Any? = null,
        contentType: ContentType? = settings.contentType
    ): HttpClientResponse =
        send(HttpClientRequest(PATCH, path = path, body = body ?: "", contentType = contentType))
}
