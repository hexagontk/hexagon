package com.hexagonkt.examples

import com.hexagonkt.client.get
import org.testng.annotations.Test

/**
 * TODO .
 */
@Test class SparkExamplesTest {
    fun `Hello World`() {
        hello.run()

        val responseBody = get("http://localhost:${hello.runtimePort}/hello").responseBody
        println(responseBody)
        assert(responseBody == "Hello World")

        hello.stop()
    }

    fun `Error handling`() {
        errorHandling.run()

        val responseBody = get("http://localhost:${errorHandling.runtimePort}/error")
        println(responseBody.responseBody)
        println(responseBody.statusCode)

        val responseBodyMissing = get("http://localhost:${errorHandling.runtimePort}/missing")
        println(responseBodyMissing.responseBody)
        println(responseBodyMissing.statusCode)
//        assert(responseBody == "Hello World")

        errorHandling.stop()
    }
}
