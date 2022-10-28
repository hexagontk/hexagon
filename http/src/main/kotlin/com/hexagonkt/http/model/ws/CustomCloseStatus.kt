package com.hexagonkt.http.model.ws

data class CustomCloseStatus(
    override val code: Int
) : WsCloseStatus
