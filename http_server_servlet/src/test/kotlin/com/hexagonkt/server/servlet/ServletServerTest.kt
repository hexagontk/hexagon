package com.hexagonkt.http.server.servlet

import com.hexagonkt.http.client.Client
import com.hexagonkt.http.server.router
import org.eclipse.jetty.server.Server as JettyServer
import org.eclipse.jetty.webapp.WebAppContext
import org.testng.annotations.AfterClass
import org.testng.annotations.BeforeClass
import org.testng.annotations.Test
import java.net.InetSocketAddress
import javax.servlet.annotation.WebListener

@Test class ServletServerTest {
    @WebListener class Serv : ServletServer() {
        override fun createRouter() = router {
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
        context.addEventListener(Serv())

        jettyServer.handler = context
        jettyServer.start()
    }

    fun `Servlet server starts`() {
        val response = Client("http://127.0.0.1:9897").get("/")
        assert(response.responseBody == "Hello Servlet!")
    }
}
