package com.hexagonkt.http.client.jetty

import com.hexagonkt.http.client.model.ws.WsClientSession
import com.hexagonkt.http.model.ws.WsCloseStatus
import org.eclipse.jetty.websocket.api.Session
import java.net.URI
import java.nio.ByteBuffer

class JettyClientWsSession(
    override val uri: URI,
    private val session: Session
) : WsClientSession {

    override fun send(data: ByteArray) {
        session.remote.sendBytes(ByteBuffer.wrap(data))
    }

    override fun send(text: String) {
        session.remote.sendString(text)
    }

    override fun ping(data: ByteArray) {
        session.remote.sendPing(ByteBuffer.wrap(data))
    }

    override fun pong(data: ByteArray) {
        session.remote.sendPong(ByteBuffer.wrap(data))
    }

    override fun close(status: WsCloseStatus, reason: String) {
        session.close(status.code, reason)
    }
}
