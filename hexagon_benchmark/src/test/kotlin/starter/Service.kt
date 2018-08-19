package starter

import java.time.LocalDateTime.now
import javax.servlet.annotation.WebListener

import com.hexagonkt.server.*
import com.hexagonkt.server.jetty.JettyServletAdapter
import com.hexagonkt.server.servlet.ServletServer

/**
 * Routes setup. It is in its own method to be able to call it for the Webapp and from the Service.
 */
val router = router {
    before {
        response.addHeader("Server", "Servlet/3.1")
        response.addHeader("Transfer-Encoding", "chunked")
        response.addHeader("Date", httpDate(now()))
    }

    get("/text") { ok("Hello, World!", "text/plain") }
}

/**
 * Main Webapp class.
 */
@Suppress("unused")
@WebListener class Web : ServletServer (router)

val server = Server(serverEngine = JettyServletAdapter(), router = router)

/**
 * Start the service from the command line.
 */
fun main(vararg args: String) {
    server.run()
}
