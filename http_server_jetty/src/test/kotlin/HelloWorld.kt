package com.hexagonkt.http.server.jetty

import com.hexagonkt.http.server.Server

lateinit var server: Server

fun main() {
    server = Server(JettyServletAdapter()) {
        get("/hello") {
            ok("Hello World!")
        }
    }

    server.start()
}
