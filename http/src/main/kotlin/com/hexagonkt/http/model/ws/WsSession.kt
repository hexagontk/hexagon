package com.hexagonkt.http.model.ws

import com.hexagonkt.http.model.ws.CloseStatus.NORMAL

interface WsSession {
    fun send(data: ByteArray)
    fun send(text: String)
    fun ping(data: ByteArray)
    fun close(status: WsCloseStatus = NORMAL, reason: String = "")
}
