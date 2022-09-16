package com.hexagonkt.http.server.jetty.poc

import org.eclipse.jetty.websocket.api.Session
import org.eclipse.jetty.websocket.api.StatusCode
import org.eclipse.jetty.websocket.api.WebSocketAdapter
import org.eclipse.jetty.websocket.client.WebSocketClient
import org.junit.jupiter.api.Test
import java.net.URI
import java.util.*
import java.util.concurrent.Future
import kotlin.test.assertEquals

internal class JettyTest {

    @Test fun `HTTP call works OK`() {

        main()

        val client = WebSocketClient()
        client.start()

        var result = ""
        val adapter = object : WebSocketAdapter() {
            override fun onWebSocketText(message: String) {
                result = message
                if (message.lowercase(Locale.US).contains("bye")) {
                    session.close(StatusCode.NORMAL, "Thanks")
                }
            }
        }

        val fut: Future<Session> = client.connect(adapter, URI("ws://localhost:8080/ws"))
        val session: Session = fut.get()

        session.remote.sendString("Hello")
        Thread.sleep(200)
        assertEquals("Hello_", result)
        session.remote.sendString("Goodbye")
        Thread.sleep(200)
        session.close()
        assertEquals("Goodbye_", result)
        server.stop()
    }
}
