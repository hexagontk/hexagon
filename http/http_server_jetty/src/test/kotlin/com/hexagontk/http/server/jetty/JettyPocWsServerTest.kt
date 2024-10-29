package com.hexagontk.http.server.jetty

import com.hexagontk.core.urlOf
import jakarta.servlet.*
import org.eclipse.jetty.server.Server
import org.eclipse.jetty.server.ServerConnector
import org.eclipse.jetty.ee10.servlet.ServletContextHandler
import java.util.*
import com.hexagontk.http.client.HttpClient
import com.hexagontk.http.client.HttpClientSettings
import com.hexagontk.http.client.jetty.ws.JettyWsHttpClient
import com.hexagontk.http.model.HttpMethod.GET
import com.hexagontk.http.model.ws.NORMAL
import com.hexagontk.http.server.HttpServer
import com.hexagontk.http.handlers.AfterHandler
import com.hexagontk.http.handlers.PathHandler
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
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.condition.DisabledOnOs
import org.junit.jupiter.api.condition.OS.WINDOWS
import kotlin.test.assertEquals

@DisabledOnOs(WINDOWS) // TODO Investigate what makes this test fail on Windows
internal class JettyPocWsServerTest {

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
        val httpClient = HttpClient(JettyWsHttpClient(), settings)
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
            JettyServletHttpServer(),
            PathHandler(
                AfterHandler { ok() },
                AfterHandler(GET) { ok() }
            )
        )
    }
}
