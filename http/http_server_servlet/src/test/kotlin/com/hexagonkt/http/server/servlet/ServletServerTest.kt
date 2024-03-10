package com.hexagonkt.http.server.servlet

import com.hexagonkt.core.urlOf
import com.hexagonkt.http.client.HttpClient
import com.hexagonkt.http.client.HttpClientSettings
import com.hexagonkt.http.client.jetty.JettyClientAdapter
import com.hexagonkt.http.model.NOT_FOUND_404
import com.hexagonkt.http.handlers.path
import jakarta.servlet.MultipartConfigElement
import org.eclipse.jetty.ee10.webapp.WebAppContext
import org.junit.jupiter.api.AfterAll
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestInstance
import org.junit.jupiter.api.TestInstance.Lifecycle.PER_CLASS
import java.net.InetSocketAddress
import jakarta.servlet.annotation.WebListener
import org.eclipse.jetty.ee10.servlet.DefaultServlet
import org.eclipse.jetty.ee10.servlet.ServletHolder
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
        val context = WebAppContext()
        context.contextPath = "/"
        context.war = "."
        context.addEventListener(WebAppServer())

        val servletHolder = ServletHolder("default", DefaultServlet())
        servletHolder.registration.setMultipartConfig(MultipartConfigElement("/tmp"))
        context.addServlet(servletHolder, "/*")

        jettyServer.handler = context
        jettyServer.start()
    }

    @AfterAll fun shutdown() {
        jettyServer.stopAtShutdown = true
        jettyServer.stop()
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
