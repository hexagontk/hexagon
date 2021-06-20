package com.hexagonkt.http.server.servlet

import com.hexagonkt.http.client.Client
import com.hexagonkt.http.client.ahc.AhcAdapter
import com.hexagonkt.http.server.Router
import com.hexagonkt.http.server.ServerSettings
import io.mockk.Runs
import io.mockk.every
import io.mockk.just
import io.mockk.mockk
import org.eclipse.jetty.webapp.WebAppContext
import org.junit.jupiter.api.AfterAll
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestInstance
import org.junit.jupiter.api.TestInstance.Lifecycle.PER_CLASS
import java.net.InetSocketAddress
import javax.servlet.FilterChain
import javax.servlet.ServletRequest
import javax.servlet.ServletResponse
import javax.servlet.annotation.WebListener
import kotlin.test.assertFailsWith
import org.eclipse.jetty.server.Server as JettyServer

@TestInstance(PER_CLASS)
internal class ServletServerTest {

    @WebListener
    class WebAppServer : ServletServer(
        Router {
            get { ok("Hello Servlet!") }
        }
    )

    private val jettyServer = JettyServer(InetSocketAddress("127.0.0.1", 9897))

    @AfterAll fun shutdown() {
        jettyServer.stopAtShutdown = true
        jettyServer.stop()
    }

    @BeforeAll fun `Run server`() {
        val context = WebAppContext()
        context.contextPath = "/"
        context.war = "."
        context.addEventListener(WebAppServer())

        jettyServer.handler = context
        jettyServer.start()
    }

    @Test fun `Servlet server starts`() {
        val response = Client(AhcAdapter(), "http://127.0.0.1:9897").get("/")
        assert(response.body == "Hello Servlet!")
    }

    @Test fun `Invalid types in filter calls raise an exception`() {
        val filter = ServletFilter(emptyList(), ServerSettings())
        val request = mockk<ServletRequest>()
        val response = mockk<ServletResponse>()
        val chain = mockk<FilterChain>()

        every { request.setAttribute(any(), any()) } just Runs
        assertFailsWith<IllegalStateException> {
            filter.doFilter(request, response, chain)
        }
    }
}
