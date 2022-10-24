package com.hexagonkt.http.model

interface WsSession {
    fun send(data: ByteArray)
    fun send(text: String)

    val httpRequest: HttpRequest
}
