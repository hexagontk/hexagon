package com.hexagonkt.http.client.jetty.ws

import com.hexagonkt.http.model.ws.WsSession
import org.eclipse.jetty.websocket.api.Callback
import org.eclipse.jetty.websocket.api.Session
import org.eclipse.jetty.websocket.api.annotations.OnWebSocketClose
import org.eclipse.jetty.websocket.api.annotations.OnWebSocketMessage
import org.eclipse.jetty.websocket.api.annotations.OnWebSocketOpen
import org.eclipse.jetty.websocket.api.annotations.WebSocket
import java.net.URI
import java.nio.ByteBuffer

@WebSocket
@Suppress("UNUSED_PARAMETER", "UNUSED") // Signatures must match the annotation expected parameters
class JettyWebSocketAdapter(
    private val uri: URI,
    private val onConnect: WsSession.() -> Unit,
    private val onBinary: WsSession.(data: ByteArray) -> Unit,
    private val onText: WsSession.(text: String) -> Unit,
    private val onPing: WsSession.(data: ByteArray) -> Unit,
    private val onPong: WsSession.(data: ByteArray) -> Unit,
    private val onClose: WsSession.(status: Int, reason: String) -> Unit,
) {
    private lateinit var session: Session
    private val wsSession by lazy { JettyClientWsSession(uri, session) }

    @OnWebSocketMessage
    fun onWebSocketText(session: Session, message: String) {
        wsSession.onText(message)
    }

    // TODO Handle 'onPing' and 'onPong' (messages probably arriving here)
    @OnWebSocketMessage
    fun onWebSocketBinary(session: Session, payload: ByteBuffer, callback: Callback) {
        wsSession.onBinary(payload.array())
    }

    @OnWebSocketClose
    fun onWebSocketClose(session: Session, statusCode: Int, reason: String) {
        wsSession.onClose(statusCode, reason)
    }

    @OnWebSocketOpen
    fun onWebSocketConnect(connectSession: Session) {
        session = connectSession
        wsSession.onConnect()
    }
}
