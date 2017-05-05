package co.there4.hexagon.server.engine.servlet

import co.there4.hexagon.client.Client
import co.there4.hexagon.server.Router
import org.eclipse.jetty.server.Server as JettyServer
import org.eclipse.jetty.webapp.WebAppContext
import org.testng.annotations.AfterClass
import org.testng.annotations.BeforeClass
import org.testng.annotations.Test
import java.net.InetSocketAddress
import javax.servlet.annotation.WebListener

@Test class ServletServerTest {
    @WebListener class Serv : ServletServer() {
        override fun Router.initRoutes() {
            get { ok("Hello Servlet!") }
        }
    }

    private val jettyServer = JettyServer(InetSocketAddress("127.0.0.1", 9897))

    @AfterClass fun shutdown() {
        jettyServer.stopAtShutdown = true
        jettyServer.stop()
    }

    @BeforeClass fun run_server() {
        val context = WebAppContext()
        context.contextPath = "/"
        context.war = "."
        context.addEventListener(Serv())

        jettyServer.handler = context
        jettyServer.start()
    }

    fun servlet_server_starts() {
        val response = Client("http://127.0.0.1:9897").get("/")
        assert(response.responseBody == "Hello Servlet!")
    }
}
