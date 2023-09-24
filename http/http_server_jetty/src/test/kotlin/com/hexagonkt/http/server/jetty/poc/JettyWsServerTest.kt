package com.hexagonkt.http.server.jetty.poc

import com.hexagonkt.core.urlOf
import jakarta.servlet.*
import org.eclipse.jetty.server.Server
import org.eclipse.jetty.server.ServerConnector
import org.eclipse.jetty.ee10.servlet.ServletContextHandler
import java.util.*
import com.hexagonkt.http.client.HttpClient
import com.hexagonkt.http.client.HttpClientSettings
import com.hexagonkt.http.client.jetty.ws.JettyWsClientAdapter
import com.hexagonkt.http.model.HttpMethod.GET
import com.hexagonkt.http.model.ws.NORMAL
import com.hexagonkt.http.server.HttpServer
import com.hexagonkt.http.handlers.AfterHandler
import com.hexagonkt.http.handlers.PathHandler
import com.hexagonkt.http.server.jetty.JettyServletAdapter
import jakarta.websocket.CloseReason
import jakarta.websocket.CloseReason.CloseCodes
import jakarta.websocket.OnClose
import jakarta.websocket.OnError
import jakarta.websocket.OnMessage
import jakarta.websocket.OnOpen
import jakarta.websocket.server.ServerEndpoint
import jakarta.websocket.Session
import org.eclipse.jetty.ee10.websocket.jakarta.server.config.JakartaWebSocketServletContainerInitializer
import org.junit.jupiter.api.condition.DisabledInNativeImage
import kotlin.test.Test
import org.junit.jupiter.api.condition.DisabledOnOs
import org.junit.jupiter.api.condition.OS.WINDOWS
import kotlin.test.assertEquals

@DisabledOnOs(WINDOWS) // TODO Investigate what makes this test fail on Windows
internal class JettyWsServerTest {

    @ServerEndpoint("/ws")
    @Suppress("UNUSED_PARAMETER", "UNUSED") // Signatures must match the annotation expected parameters
    class Ws {
        @OnOpen fun onWebSocketConnect(session: Session) {
            println("Socket Connected: $session")
        }

        @OnMessage fun onWebSocketText(message: String, session: Session) { println("Received TEXT message: $message")
            session.basicRemote.sendText(message + "_")
            if (message.lowercase(Locale.US).contains("bye")) {
                session.close(CloseReason(CloseCodes.NORMAL_CLOSURE, "Thanks"))
            }
        }

        @OnClose fun onWebSocketClose(session: Session, closeReason: CloseReason) {
            println("Socket Closed: [${closeReason.closeCode}] ${closeReason.reasonPhrase}")
        }

        @OnError fun onWebSocketError(cause: Throwable) {
            cause.printStackTrace(System.err)
        }
    }

    private lateinit var server: Server

    private fun startServer() {
        val context = ServletContextHandler().apply { contextPath = "/" }

        JakartaWebSocketServletContainerInitializer.configure(context) { _, wsContainer ->
            wsContainer.defaultMaxTextMessageBufferSize = 65535
            wsContainer.addEndpoint(Ws::class.java)
        }

        val filter = context.servletContext.addFilter("filter") { _, response, _ ->
            response?.writer?.write("Hi")
        }

        val dispatcherTypes = EnumSet.allOf(DispatcherType::class.java)
        filter.addMappingForUrlPatterns(dispatcherTypes, true, "/text/*")

        server = Server()
        server.addConnector(ServerConnector(server).apply { port = 8080 })
        server.handler = context
        server.stopAtShutdown = true
        server.start()
    }

    @Test
    @DisabledInNativeImage
    fun `WS call works OK`() {
        startServer()

        var result = ""

        val settings = HttpClientSettings(urlOf("http://localhost:8080"))
        val httpClient = HttpClient(JettyWsClientAdapter(), settings)
        httpClient.start()
        val ws = httpClient.ws(
            path = "/ws",
            onText = {
                result = it
                if (it.lowercase().contains("bye")) {
                    close(NORMAL, "Thanks")
                }
            }
        )

        ws.send("Hello")
        Thread.sleep(800)
        assertEquals("Hello_", result)
        ws.send("Goodbye")
        Thread.sleep(800)
        ws.close()
        assertEquals("Goodbye_", result)

        httpClient.close()
    }

    @Test fun `Simple server example`() {
        HttpServer(
            JettyServletAdapter(),
            PathHandler(
                AfterHandler { ok() },
                AfterHandler(GET) { ok() }
            )
        )
    }
}
