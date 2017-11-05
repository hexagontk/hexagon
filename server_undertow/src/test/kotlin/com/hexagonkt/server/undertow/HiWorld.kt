package com.hexagonkt.server.undertow

fun main(vararg args: String) {
    serve {
        get("/bye") { "Good Bye!" }
        get { "Hi!" }
    }
}
