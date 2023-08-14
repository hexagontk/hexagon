package com.hexagonkt.http.client.jetty

import com.hexagonkt.http.model.HttpRequestPort
import com.hexagonkt.http.model.ws.WsSession
import org.eclipse.jetty.websocket.api.Callback.NOOP
import org.eclipse.jetty.websocket.api.Session
import java.net.URI
import java.nio.ByteBuffer

class JettyClientWsSession(
    override val uri: URI,
    private val session: Session
) : WsSession {

    override val attributes: Map<*, *>
        get() = throw UnsupportedOperationException()
    override val request: HttpRequestPort
        get() = throw UnsupportedOperationException()
    override val exception: Exception
        get() = throw UnsupportedOperationException()
    override val pathParameters: Map<String, String>
        get() = throw UnsupportedOperationException()

    override fun send(data: ByteArray) {
        session.sendBinary(ByteBuffer.wrap(data), NOOP)
    }

    override fun send(text: String) {
        session.sendText(text, NOOP)
    }

    override fun ping(data: ByteArray) {
        session.sendPing(ByteBuffer.wrap(data), NOOP)
    }

    override fun pong(data: ByteArray) {
        session.sendPong(ByteBuffer.wrap(data), NOOP)
    }

    override fun close(status: Int, reason: String) {
        session.close(status, reason, NOOP)
    }
}
