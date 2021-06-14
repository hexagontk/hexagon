package com.hexagonkt.http.server.jetty.examples

import com.hexagonkt.logging.logger
import com.hexagonkt.http.httpDate
import com.hexagonkt.http.server.Server
import com.hexagonkt.http.server.ServerPort
import com.hexagonkt.http.server.jetty.JettyServletAdapter
import com.hexagonkt.injection.InjectionManager

/**
 * [InjectionManager] instance to which an instance of [JettyServletAdapter]
 * is bound for dependency Injection.
 */
internal val injector = InjectionManager.module.apply {
    clear()
    bind<ServerPort>(JettyServletAdapter()) // Bind Jetty server to HTTP Server Port
}

/**
 * Service server. Adapter is injected.
 */
internal val server: Server = Server {
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
internal fun main() {
    logger.info { injector }
    server.start()
}
