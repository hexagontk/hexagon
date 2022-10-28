package com.hexagonkt.http.client.model.ws

import com.hexagonkt.http.model.ws.WsSession
import java.net.URI

interface WsClientSession : WsSession {
    val uri: URI
}
