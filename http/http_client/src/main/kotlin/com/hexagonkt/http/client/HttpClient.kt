package com.hexagonkt.http.client

import com.hexagonkt.http.handlers.HttpContext
import com.hexagonkt.http.handlers.HttpHandler
import com.hexagonkt.http.handlers.OnHandler
import com.hexagonkt.http.handlers.path
import com.hexagonkt.http.model.HttpRequest
import com.hexagonkt.http.model.HttpResponsePort
import com.hexagonkt.http.model.*
import com.hexagonkt.http.model.HttpMethod.*
import com.hexagonkt.http.model.ws.WsSession
import java.io.Closeable
import java.util.concurrent.Flow.Publisher

/**
 * Client to use other REST services.
 */
class HttpClient(
    private val adapter: HttpClientPort,
    val settings: HttpClientSettings = HttpClientSettings(),
    val handler: HttpHandler? = null,
) : Closeable {

    private val rootHandler: HttpHandler? =
        handler?.let {
            val sendHandler = OnHandler("*") { send(adapter.send(request)) }
            path("*", listOf(it, sendHandler))
        }

    var cookies: List<Cookie> = emptyList()

    override fun close() {
        stop()
    }

    fun cookiesMap(): Map<String, Cookie> =
        cookies.associateBy { it.name }

    fun start() {
        check(!started()) { "HTTP client is already started" }
        adapter.startUp(this)
    }

    fun stop() {
        check(started()) { "HTTP client *MUST BE STARTED* before shut-down" }
        adapter.shutDown()
    }

    fun started(): Boolean =
        adapter.started()

    /**
     * Synchronous execution.
     */
    fun send(request: HttpRequest, attributes: Map<String, Any> = emptyMap()): HttpResponsePort =
        if (!started())
            error("HTTP client *MUST BE STARTED* before sending requests")
        else
            rootHandler
                ?.process(request, attributes)
                ?.let {
                    if (it.exception != null) throw it.exception as Exception
                    else it.response
                }
                ?: adapter.send(request)

    fun sse(request: HttpRequest): Publisher<ServerEvent> =
        if (!started()) error("HTTP client *MUST BE STARTED* before sending requests")
        else adapter.sse(request)

    fun sse(path: String): Publisher<ServerEvent> =
        sse(HttpRequest(path = path))

    fun ws(
        path: String,
        onConnect: WsSession.() -> Unit = {},
        onBinary: WsSession.(data: ByteArray) -> Unit = {},
        onText: WsSession.(text: String) -> Unit = {},
        onPing: WsSession.(data: ByteArray) -> Unit = {},
        onPong: WsSession.(data: ByteArray) -> Unit = {},
        onClose: WsSession.(status: Int, reason: String) -> Unit = { _, _ -> },
    ): WsSession =
        if (!started()) error("HTTP client *MUST BE STARTED* before connecting to WS")
        else adapter.ws(path, onConnect, onBinary, onText, onPing, onPong, onClose)

    fun request (block: HttpClient.() -> Unit) {
        if (!started())
            start()

        use(block)
    }

    fun get(
        path: String = "",
        headers: Headers = Headers(),
        body: Any? = null,
        contentType: ContentType? = settings.contentType): HttpResponsePort =
            send(
                HttpRequest(
                    method = GET,
                    path = path,
                    body = body ?: "",
                    headers = headers,
                    contentType = contentType)
            )

    fun head(path: String = "", headers: Headers = Headers()): HttpResponsePort =
        send(HttpRequest(HEAD, path = path, body = ByteArray(0), headers = headers))

    fun post(
        path: String = "",
        body: Any? = null,
        contentType: ContentType? = settings.contentType
    ): HttpResponsePort =
        send(HttpRequest(POST, path = path, body = body ?: "", contentType = contentType))

    fun put(
        path: String = "",
        body: Any? = null,
        contentType: ContentType? = settings.contentType
    ): HttpResponsePort =
        send(HttpRequest(PUT, path = path, body = body ?: "", contentType = contentType))

    fun delete(
        path: String = "",
        body: Any? = null,
        contentType: ContentType? = settings.contentType
    ): HttpResponsePort =
        send(HttpRequest(DELETE, path = path, body = body ?: "", contentType = contentType))

    fun trace(
        path: String = "",
        body: Any? = null,
        contentType: ContentType? = settings.contentType
    ): HttpResponsePort =
        send(HttpRequest(TRACE, path = path, body = body ?: "", contentType = contentType))

    fun options(
        path: String = "",
        body: Any? = null,
        headers: Headers = Headers(),
        contentType: ContentType? = settings.contentType
    ): HttpResponsePort =
        send(
            HttpRequest(
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
    ): HttpResponsePort =
        send(HttpRequest(PATCH, path = path, body = body ?: "", contentType = contentType))

    private fun HttpHandler.process(
        request: HttpRequestPort, attributes: Map<String, Any>
    ): HttpContext =
        HttpContext(HttpCall(request = request), handlerPredicate, attributes = attributes)
            .let { context ->
                if (handlerPredicate(context)) process(context) as HttpContext
                else context
            }
}
