package com.hexagonkt.starters

import com.hexagonkt.helpers.logger
import com.hexagonkt.http.httpDate
import com.hexagonkt.http.server.Server
import com.hexagonkt.http.server.ServerPort
import com.hexagonkt.http.server.jetty.JettyServletAdapter
import com.hexagonkt.injection.InjectionManager

val injector = InjectionManager.apply {
    bindObject<ServerPort>(JettyServletAdapter()) // Bind Jetty server to HTTP Server Port
}

/**
 * Service server. Adapter is injected.
 */
val server: Server = Server {
    before {
        response.headers["Date"] = httpDate()
    }

    get("/hello/{name}") {
        ok("Hello, ${pathParameters["name"]}!", "text/plain")
    }
}

/**
 * Start the service from the command line.
 */
fun main() {
    logger.info { injector }
    server.start()
}
