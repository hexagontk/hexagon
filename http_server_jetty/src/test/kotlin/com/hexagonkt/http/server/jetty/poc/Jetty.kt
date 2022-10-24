package com.hexagonkt.http.server.jetty.poc

import jakarta.servlet.*
import org.eclipse.jetty.server.Server
import org.eclipse.jetty.server.ServerConnector
import org.eclipse.jetty.servlet.ServletContextHandler
import org.eclipse.jetty.websocket.api.Session
import org.eclipse.jetty.websocket.api.StatusCode
import org.eclipse.jetty.websocket.api.WebSocketAdapter
import org.eclipse.jetty.websocket.server.config.JettyWebSocketServletContainerInitializer
import java.util.*

lateinit var server: Server

fun main() {
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
