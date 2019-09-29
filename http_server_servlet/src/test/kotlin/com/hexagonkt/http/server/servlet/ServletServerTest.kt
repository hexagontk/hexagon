package com.hexagonkt.http.server.servlet

import com.hexagonkt.http.client.Client
import com.hexagonkt.http.server.Router
import io.mockk.Runs
import io.mockk.every
import io.mockk.just
import io.mockk.mockk
import org.eclipse.jetty.server.Server as JettyServer
import org.eclipse.jetty.webapp.WebAppContext
import org.testng.annotations.AfterClass
import org.testng.annotations.BeforeClass
import org.testng.annotations.Test
import java.net.InetSocketAddress
import javax.servlet.FilterChain
import javax.servlet.ServletRequest
import javax.servlet.ServletResponse
import javax.servlet.annotation.WebListener

@Test class ServletServerTest {
    @WebListener
    class WebAppServer : ServletServer() {
        override fun createRouter() = Router {
            get { ok("Hello Servlet!") }
        }
    }

    private val jettyServer = JettyServer(InetSocketAddress("127.0.0.1", 9897))

    @AfterClass fun shutdown() {
        jettyServer.stopAtShutdown = true
        jettyServer.stop()
    }

    @BeforeClass fun `Run server`() {
        val context = WebAppContext()
        context.contextPath = "/"
        context.war = "."
        context.addEventListener(WebAppServer())

        jettyServer.handler = context
        jettyServer.start()
    }

    @Test fun `Servlet server starts`() {
        val response = Client("http://127.0.0.1:9897").get("/")
        assert(response.responseBody == "Hello Servlet!")
    }

    @Test(expectedExceptions = [ IllegalStateException::class ])
    fun `Invalid types in filter calls raise an exception`() {
        val filter = ServletFilter(emptyList())
        val request = mockk<ServletRequest>()
        val response = mockk<ServletResponse>()
        val chain = mockk<FilterChain>()

        every { request.setAttribute(any(), any()) } just Runs
        filter.doFilter(request, response, chain)
    }
}
