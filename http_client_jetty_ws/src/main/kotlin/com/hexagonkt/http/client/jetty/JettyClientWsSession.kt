package com.hexagonkt.http.client.jetty

import com.hexagonkt.http.model.HttpRequestPort
import com.hexagonkt.http.model.ws.WsSession
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

    override fun close(status: Int, reason: String) {
        session.close(status, reason)
    }
}
