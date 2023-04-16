package com.hexagonkt.http.client

import com.hexagonkt.http.model.HttpRequestPort
import com.hexagonkt.http.model.HttpResponsePort
import com.hexagonkt.http.model.ServerEvent
import com.hexagonkt.http.model.ws.WsSession
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
