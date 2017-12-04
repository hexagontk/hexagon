package com.hexagonkt.examples

import com.hexagonkt.server.Server
import com.hexagonkt.server.undertow.server

val hello: Server = server {
    get("/hello") { "Hello World" }
}

// TODO Define Handling order
val errorHandling = server {
    get("/error") {
        throw IllegalStateException("Serious error")
    }

    error(404) {
        response.status = 500
        499 to "${request.path} is missing ($it)"
    }

    error(IllegalStateException::class) {
        response.status = 503
        response.body = "bad"
        500 to "Bad state"
    }
}
