package com.hexagonkt.http.client

import com.hexagonkt.http.client.model.HttpClientRequest
import com.hexagonkt.http.client.model.HttpClientResponse
import com.hexagonkt.http.model.ws.WsCloseStatus
import com.hexagonkt.http.model.ws.WsSession

interface HttpClientPort {

    fun startUp(client: HttpClient)

    fun shutDown()

    fun send(request: HttpClientRequest): HttpClientResponse

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
