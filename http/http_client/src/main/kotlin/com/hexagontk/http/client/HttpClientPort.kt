package com.hexagontk.http.client

import com.hexagontk.http.model.HttpRequestPort
import com.hexagontk.http.model.HttpResponsePort
import com.hexagontk.http.model.ServerEvent
import com.hexagontk.http.model.ws.WsSession
import java.util.concurrent.Flow.Publisher

interface HttpClientPort {

    fun startUp(client: HttpClient)

    fun shutDown()

    fun started(): Boolean

    fun send(request: HttpRequestPort): HttpResponsePort

    fun sse(request: HttpRequestPort): Publisher<ServerEvent>

    fun ws(
        path: String,
        onConnect: WsSession.() -> Unit,
        onBinary: WsSession.(data: ByteArray) -> Unit,
        onText: WsSession.(text: String) -> Unit,
        onPing: WsSession.(data: ByteArray) -> Unit = {},
        onPong: WsSession.(data: ByteArray) -> Unit = {},
        onClose: WsSession.(status: Int, reason: String) -> Unit,
    ): WsSession
}