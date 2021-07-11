package com.hexagonkt.http.server.jetty.examples

import com.hexagonkt.http.httpDate
import com.hexagonkt.http.server.Server
import com.hexagonkt.http.server.jetty.JettyServletAdapter

/**
 * Service server.
 */
internal val server: Server = Server(JettyServletAdapter()) {
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
    server.start()
}
