package com.hexagonkt.http.client.jetty

import com.hexagonkt.http.model.ws.CloseStatus
import com.hexagonkt.http.model.ws.WsCloseStatus
import com.hexagonkt.http.model.ws.WsSession
import org.eclipse.jetty.websocket.api.Session
import org.eclipse.jetty.websocket.api.WebSocketAdapter

class JettyWebSocketAdapter(
    private val onConnect: WsSession.() -> Unit,
    private val onBinary: WsSession.(data: ByteArray) -> Unit,
    private val onText: WsSession.(text: String) -> Unit,
    private val onClose: WsSession.(status: WsCloseStatus, reason: String) -> Unit,
) : WebSocketAdapter() {

    private val wsSession by lazy { JettyClientWsSession(session) }

    override fun onWebSocketText(message: String) {
        wsSession.onText(message)
    }

    override fun onWebSocketBinary(payload: ByteArray, offset: Int, len: Int) {
        wsSession.onBinary(payload)
    }

    override fun onWebSocketClose(statusCode: Int, reason: String) {
        wsSession.onClose(CloseStatus.valueOf(statusCode), reason)
        super.onWebSocketClose(statusCode, reason)
    }

    override fun onWebSocketConnect(sess: Session) {
        super.onWebSocketConnect(sess)
        wsSession.onConnect()
    }

    override fun onWebSocketError(cause: Throwable?) {
        super.onWebSocketError(cause)
    }
}
