package com.hexagonkt.http.server.handlers

import com.hexagonkt.core.handlers.Handler

interface WsHandler : ServerHandler, Handler<WsSession> {
    fun addPrefix(prefix: String): WsHandler
}
