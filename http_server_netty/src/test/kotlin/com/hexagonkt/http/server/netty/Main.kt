package com.hexagonkt.http.server.netty

import com.hexagonkt.http.server.HttpServer
import com.hexagonkt.http.server.HttpServerSettings

fun main() {

    val s = HttpServer(NettyAdapter(), HttpServerSettings(bindPort = 0)) {
        get("/text") { ok("Hello!") }
    }

    s.start()
//    s.stop()
}