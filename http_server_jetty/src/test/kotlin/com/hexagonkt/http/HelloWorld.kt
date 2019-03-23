package com.hexagonkt.http

import com.hexagonkt.http.server.Server
import com.hexagonkt.http.server.jetty.JettyServletAdapter

val server = Server(JettyServletAdapter()) {
    get("/hello") {
        ok("Hello World!")
    }
}

fun main() {
    server.start()
}
