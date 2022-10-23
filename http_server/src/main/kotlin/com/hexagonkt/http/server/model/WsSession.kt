package com.hexagonkt.http.server.model

interface WsSession {
    fun send(data: ByteArray)
    fun send(text: String)

    val httpRequest: HttpServerRequestPort
}
