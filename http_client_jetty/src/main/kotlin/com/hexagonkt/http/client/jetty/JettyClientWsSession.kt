package com.hexagonkt.http.client.jetty

import com.hexagonkt.http.model.ws.WsCloseStatus
import com.hexagonkt.http.model.ws.WsSession
import org.eclipse.jetty.websocket.api.Session
import java.nio.ByteBuffer

class JettyClientWsSession(
    private val session: Session
) : WsSession {

    override fun send(data: ByteArray) {
        session.remote.sendBytes(ByteBuffer.wrap(data))
    }

    override fun send(text: String) {
        session.remote.sendString(text)
    }

    override fun ping(data: ByteArray) {
        session.remote.sendPing(ByteBuffer.wrap(data))
    }

    override fun close(status: WsCloseStatus, reason: String) {
        session.close(status.code, reason)
    }
}
