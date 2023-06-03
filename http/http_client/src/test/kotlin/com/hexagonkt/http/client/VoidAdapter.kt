package com.hexagonkt.http.client

import com.hexagonkt.http.model.*
import com.hexagonkt.http.model.ws.WsSession
import java.net.URI
import java.util.concurrent.Flow.Publisher
import java.util.concurrent.SubmissionPublisher

object VoidAdapter : HttpClientPort {
    internal val eventPublisher = SubmissionPublisher<ServerEvent>()
    var started: Boolean = false

    override fun startUp(client: HttpClient) {
        started = true
    }

    override fun shutDown() {
        started = false
    }

    override fun started() =
        started

    override fun send(request: HttpRequestPort): HttpResponsePort =
        HttpResponse(
            headers = request.headers + Header("-path-", request.path),
            body = request.body,
            contentType = request.contentType,
        )

    override fun sse(request: HttpRequestPort): Publisher<ServerEvent> =
        eventPublisher

    override fun ws(
        path: String,
        onConnect: WsSession.() -> Unit,
        onBinary: WsSession.(data: ByteArray) -> Unit,
        onText: WsSession.(text: String) -> Unit,
        onPing: WsSession.(data: ByteArray) -> Unit,
        onPong: WsSession.(data: ByteArray) -> Unit,
        onClose: WsSession.(status: Int, reason: String) -> Unit
    ): WsSession =
        object : WsSession {
            override val uri: URI = URI(path)
            override val attributes: Map<*, *> = emptyMap<Any, Any>()
            override val request: HttpRequestPort = HttpRequest()
            override val exception: Exception? = null
            override val pathParameters: Map<String, String> = emptyMap()

            override fun send(data: ByteArray) {
                onBinary(data)
            }

            override fun send(text: String) {
                onText(text)
            }

            override fun ping(data: ByteArray) {
                onPing(data)
            }

            override fun pong(data: ByteArray) {
                onPong(data)
            }

            override fun close(status: Int, reason: String) {
                onClose(status, reason)
            }
        }
}
