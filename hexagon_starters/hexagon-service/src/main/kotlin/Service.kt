package ${group}

import java.time.LocalDateTime.now

import com.hexagonkt.server.*
import com.hexagonkt.server.jetty.JettyServletAdapter
import com.hexagonkt.settings.SettingsManager

val server: Server = server(JettyServletAdapter(), SettingsManager.settings) {
    before {
        response.addHeader("Server", "Servlet/3.1")
        response.addHeader("Transfer-Encoding", "chunked")
        response.addHeader("Date", httpDate(now()))
    }

    get("/text") { ok("Hello, World!", "text/plain") }
}

/**
 * Start the service from the command line.
 */
fun main(vararg args: String) {
    server.run()
}

