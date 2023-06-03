package com.hexagonkt.http.client.jetty

import com.hexagonkt.http.model.ws.WsSession
import org.eclipse.jetty.websocket.api.Session
import org.eclipse.jetty.websocket.api.WebSocketAdapter
import java.net.URI

class JettyWebSocketAdapter(
    private val uri: URI,
    private val onConnect: WsSession.() -> Unit,
    private val onBinary: WsSession.(data: ByteArray) -> Unit,
    private val onText: WsSession.(text: String) -> Unit,
    private val onPing: WsSession.(data: ByteArray) -> Unit,
    private val onPong: WsSession.(data: ByteArray) -> Unit,
    private val onClose: WsSession.(status: Int, reason: String) -> Unit,
) : WebSocketAdapter() {

    private val wsSession by lazy { JettyClientWsSession(uri, session) }

    override fun onWebSocketText(message: String) {
        wsSession.onText(message)
    }

    // TODO Handle 'onPing' and 'onPong' (messages probably arriving here)
    override fun onWebSocketBinary(payload: ByteArray, offset: Int, len: Int) {
        wsSession.onBinary(payload)
    }

    override fun onWebSocketClose(statusCode: Int, reason: String) {
        wsSession.onClose(statusCode, reason)
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
