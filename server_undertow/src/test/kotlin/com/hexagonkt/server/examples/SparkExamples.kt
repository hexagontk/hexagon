package com.hexagonkt.server.examples

import com.hexagonkt.server.Server
import com.hexagonkt.server.undertow.serve

fun helloWorld(): Server =
    serve {
        get("/hello") { "Hello World" }
    }

fun errorHandling(): Server =
    serve {
        get("/error") {
            throw IllegalStateException("Serious error")
        }

        get("/unhandledError") {
            throw UnsupportedOperationException("Unhandled exception")
        }

        error(IllegalStateException::class) {
            599 to "Bad error"
        }

        error(Exception::class) {
            500 to "Very bad error"
        }

        error(404) {
            "${request.path} is missing ($it)"
        }

        error(500) {
            500 to "${request.path} (${response.body})"
        }
    }
