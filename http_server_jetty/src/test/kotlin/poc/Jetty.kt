package com.hexagonkt.http.server.jetty.poc

import com.hexagonkt.core.Jvm
import com.hexagonkt.core.logging.logger
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

    logger.info { Jvm.uptime() }

//        .get("/hello") {
//            it.requestPath
//            it.requestReceiver.receiveFullBytes { _, _ -> } // Read body (async)
//            it.responseHeaders.put(Headers.CONTENT_TYPE, "text/plain")
//            it.queryString.info()
//            it.requestPath.info()
//            it.relativePath.info()
//            it.resolvedPath.info()
//            it.responseSender.send("Hello World")
//        }
//
//        .get("/bye") {
//            it.responseHeaders.put(Headers.CONTENT_TYPE, "text/plain")
//            it.responseSender.send("Cruel World")
//        }
//
//        .addPrefixPath("/ws", websocket { exchange, channel ->
//            exchange.requestURI.info()
//            run {
//                channel.receiveSetter.set(object : AbstractReceiveListener() {
//                    override fun onFullTextMessage(
//                        channel: WebSocketChannel,
//                        message: BufferedTextMessage
//                    ) {
//                        val messageData = message.data
//                        for (session in channel.peerConnections) {
//                            WebSockets.sendText(messageData, session, null)
//                        }
//                        WebSockets.sendText("$messageData!!!", channel, null)
//                    }
//                })
//                channel.resumeReceives()
//            }
//        })
}
