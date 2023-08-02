package com.hexagonkt.http.server.jetty.poc

import com.hexagonkt.core.urlOf
import jakarta.servlet.*
import org.eclipse.jetty.server.Server
import org.eclipse.jetty.server.ServerConnector
import org.eclipse.jetty.servlet.ServletContextHandler
import org.eclipse.jetty.websocket.api.Session
import org.eclipse.jetty.websocket.api.StatusCode
import org.eclipse.jetty.websocket.api.WebSocketAdapter
import org.eclipse.jetty.websocket.server.config.JettyWebSocketServletContainerInitializer
import java.util.*
import com.hexagonkt.http.client.HttpClient
import com.hexagonkt.http.client.HttpClientSettings
import com.hexagonkt.http.client.jetty.JettyWsClientAdapter
import com.hexagonkt.http.model.HttpMethod.GET
import com.hexagonkt.http.model.ws.NORMAL
import com.hexagonkt.http.server.HttpServer
import com.hexagonkt.http.handlers.AfterHandler
import com.hexagonkt.http.handlers.PathHandler
import com.hexagonkt.http.server.jetty.JettyServletAdapter
import org.junit.jupiter.api.condition.DisabledInNativeImage
import kotlin.test.Test
import org.junit.jupiter.api.condition.DisabledOnOs
import org.junit.jupiter.api.condition.OS.WINDOWS
import kotlin.test.assertEquals

@DisabledOnOs(WINDOWS) // TODO Investigate what makes this test fail on Windows
internal class JettyWsServerTest {

    private lateinit var server: Server

    private fun startServer() {
        val context = ServletContextHandler().apply { contextPath = "/" }

        JettyWebSocketServletContainerInitializer.configure(context) { _, wsContainer ->
            wsContainer.maxTextMessageSize = 65535
            wsContainer.addMapping("/ws/*") { _, _ ->
                object : WebSocketAdapter() {
                    override fun onWebSocketConnect(sess: Session) {
                        super.onWebSocketConnect(sess)
                        println("Socket Connected: $sess")
                    }

                    override fun onWebSocketText(message: String) {
                        super.onWebSocketText(message)
                        println("Received TEXT message: $message")
                        session.remote.sendString(message + "_")
                        if (message.lowercase(Locale.US).contains("bye")) {
                            session.close(StatusCode.NORMAL, "Thanks")
                        }
                    }

                    override fun onWebSocketClose(statusCode: Int, reason: String?) {
                        super.onWebSocketClose(statusCode, reason)
                        println("Socket Closed: [$statusCode] $reason")
                    }

                    override fun onWebSocketError(cause: Throwable) {
                        super.onWebSocketError(cause)
                        cause.printStackTrace(System.err)
                    }
                }
            }
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
