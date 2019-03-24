package com.hexagonkt.http

import com.hexagonkt.http.client.Client
import org.testng.annotations.AfterClass
import org.testng.annotations.BeforeClass
import org.testng.annotations.Test

@Test class HelloWorldTest() {

    private val client: Client by lazy { Client("http://localhost:${server.runtimePort}") }

    @BeforeClass fun initialize() {
        main()
    }

    @AfterClass fun shutdown() {
        server.stop()
    }

    @Test fun `Create book returns 201 and new book ID`() {
        val result = client.get("/hello")
        assert(result.responseBody == "Hello World!")
        assert(200 == result.statusCode)
    }
}
