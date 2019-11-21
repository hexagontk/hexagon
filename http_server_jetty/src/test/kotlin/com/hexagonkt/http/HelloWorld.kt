package com.hexagonkt.http

import com.hexagonkt.http.server.Server
import com.hexagonkt.http.server.ServerPort
import com.hexagonkt.http.server.jetty.JettyServletAdapter
import com.hexagonkt.http.server.serve
import com.hexagonkt.injection.InjectionManager.bindObject

lateinit var server: Server

fun main() {
    bindObject<ServerPort>(JettyServletAdapter())

    server = serve {
        get("/hello") {
            ok("Hello World!")
        }
    }
}
