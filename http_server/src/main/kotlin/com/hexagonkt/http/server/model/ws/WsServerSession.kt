package com.hexagonkt.http.server.model.ws

import com.hexagonkt.http.model.ws.WsSession
import com.hexagonkt.http.server.model.HttpServerRequestPort

interface WsServerSession : WsSession {
    val attributes: Map<*, *>
    val request: HttpServerRequestPort
    val exception: Exception?
    val pathParameters: Map<String, String>
}
