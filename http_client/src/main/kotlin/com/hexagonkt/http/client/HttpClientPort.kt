package com.hexagonkt.http.client

import com.hexagonkt.http.client.model.HttpClientRequest
import com.hexagonkt.http.client.model.HttpClientResponse
import com.hexagonkt.http.model.ServerEvent
import com.hexagonkt.http.model.ws.WsCloseStatus
import com.hexagonkt.http.model.ws.WsSession
import java.util.concurrent.Flow.Publisher

interface HttpClientPort {

    fun startUp(client: HttpClient)

    fun shutDown()

    fun started(): Boolean

    fun send(request: HttpClientRequest): HttpClientResponse

    fun sse(request: HttpClientRequest): Publisher<ServerEvent>

    fun ws(
        path: String,
        onConnect: WsSession.() -> Unit,
        onBinary: WsSession.(data: ByteArray) -> Unit,
        onText: WsSession.(text: String) -> Unit,
        onPing: WsSession.(data: ByteArray) -> Unit = {},
        onPong: WsSession.(data: ByteArray) -> Unit = {},
        onClose: WsSession.(status: WsCloseStatus, reason: String) -> Unit,
    ): WsSession
}
