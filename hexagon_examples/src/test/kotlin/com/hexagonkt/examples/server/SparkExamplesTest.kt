package com.hexagonkt.examples.server

import com.hexagonkt.client.get
import org.testng.annotations.Test

/**
 * TODO .
 */
@Test class SparkExamplesTest {
    fun `Hello World`() {
        val server = helloWorld()

        val responseBody = get("http://localhost:${server.runtimePort}/hello").responseBody
        println(responseBody)
        assert(responseBody == "Hello World")

        server.stop()
    }

    fun `Error handling`() {
        val server = errorHandling()

        val responseBody = get("http://localhost:${server.runtimePort}/error")
        assert(responseBody.responseBody == "Bad error")
        assert(responseBody.statusCode == 599)

        val responseBodyUnhandled = get("http://localhost:${server.runtimePort}/unhandledError")
        assert(responseBodyUnhandled.responseBody == "/unhandledError (Very bad error)")
        assert(responseBodyUnhandled.statusCode == 500)

        val responseBodyMissing = get("http://localhost:${server.runtimePort}/missing")
        println(responseBodyMissing.responseBody)
        println(responseBodyMissing.statusCode)
//        assert(responseBody == "Hello World")

        server.stop()
    }
}
