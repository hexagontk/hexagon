package com.hexagonkt.examples.server

import com.hexagonkt.client.get
import com.hexagonkt.server.Server
import com.hexagonkt.server.server
import com.hexagonkt.server.undertow.UndertowAdapter
import com.hexagonkt.server.undertow.serve
import org.testng.annotations.BeforeTest
import org.testng.annotations.Test

/**
 * TODO .
 */
@Test class ErrorHandlingTest {
    var server: Server = server(UndertowAdapter()) {}

    @BeforeTest fun errorHandling() {
        server = serve {
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
    }

    fun `Error handling`() {
        //val responseBody = get("http://localhost:${server.runtimePort}/error")
        //assert(responseBody.responseBody == "Bad error")
        //assert(responseBody.statusCode == 599)

        //val responseBodyUnhandled = get("http://localhost:${server.runtimePort}/unhandledError")
        //assert(responseBodyUnhandled.responseBody == "/unhandledError (Very bad error)")
        //assert(responseBodyUnhandled.statusCode == 500)

        //val responseBodyMissing = get("http://localhost:${server.runtimePort}/missing")
        //println(responseBodyMissing.responseBody)
        //println(responseBodyMissing.statusCode)
//        assert(responseBody == "Hello World")

        //server.stop()
    }
}
