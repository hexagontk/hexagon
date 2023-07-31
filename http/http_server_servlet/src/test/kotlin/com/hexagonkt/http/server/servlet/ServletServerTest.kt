package com.hexagonkt.http.server.servlet

import com.hexagonkt.core.logging.LoggingLevel.DEBUG
import com.hexagonkt.core.logging.LoggingLevel.OFF
import com.hexagonkt.core.logging.LoggingManager
import com.hexagonkt.core.urlOf
import com.hexagonkt.logging.jul.JulLoggingAdapter
import com.hexagonkt.http.client.HttpClient
import com.hexagonkt.http.client.HttpClientSettings
import com.hexagonkt.http.client.jetty.JettyClientAdapter
import com.hexagonkt.http.model.NOT_FOUND_404
import com.hexagonkt.http.handlers.path
import org.eclipse.jetty.webapp.WebAppContext
import org.junit.jupiter.api.AfterAll
import org.junit.jupiter.api.BeforeAll
import kotlin.test.Test
import org.junit.jupiter.api.TestInstance
import org.junit.jupiter.api.TestInstance.Lifecycle.PER_CLASS
import java.net.InetSocketAddress
import jakarta.servlet.annotation.WebListener
import kotlin.test.assertEquals
import kotlin.test.assertNull
import org.eclipse.jetty.server.Server as JettyServer

@TestInstance(PER_CLASS)
internal class ServletServerTest {

    @WebListener
    class WebAppServer : ServletServer(
        path {
            get {
                assertEquals(emptyList(), request.certificateChain)
                assertNull(request.certificate())
                ok("Hello Servlet!")
            }
        }
    )

    private val jettyServer = JettyServer(InetSocketAddress("127.0.0.1", 9897))

    @BeforeAll fun `Run server`() {
        LoggingManager.adapter = JulLoggingAdapter()
        LoggingManager.setLoggerLevel("com.hexagonkt", DEBUG)
        val context = WebAppContext()
        context.contextPath = "/"
        context.war = "."
        context.addEventListener(WebAppServer())

        jettyServer.handler = context
        jettyServer.start()
    }

    @AfterAll fun shutdown() {
        jettyServer.stopAtShutdown = true
        jettyServer.stop()
        LoggingManager.setLoggerLevel("com.hexagonkt", OFF)
    }

    @Test fun `Servlet server starts`() {
        val settings = HttpClientSettings(urlOf("http://127.0.0.1:9897"))
        HttpClient(JettyClientAdapter(), settings).use {
            it.start()
            assertEquals("Hello Servlet!", it.get("/").body)
            assertEquals(NOT_FOUND_404, it.post("/").status)
        }
    }
}
