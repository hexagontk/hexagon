package com.hexagontk.http.model.ws

import com.hexagontk.http.model.HttpRequestPort
import java.net.URI

interface WsSession {
    val uri: URI

    val attributes: Map<*, *>
    val request: HttpRequestPort
    val exception: Exception?
    val pathParameters: Map<String, String>

    fun send(data: ByteArray)
    fun send(text: String)
    fun ping(data: ByteArray)
    fun pong(data: ByteArray)
    fun close(status: Int = NORMAL, reason: String = "")
}
