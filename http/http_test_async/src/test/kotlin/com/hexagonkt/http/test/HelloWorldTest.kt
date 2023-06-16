package com.hexagonkt.http.test

import com.hexagonkt.core.media.TEXT_PLAIN
import com.hexagonkt.handlers.async.done
import com.hexagonkt.http.client.jetty.JettyClientAdapter
import com.hexagonkt.http.client.HttpClient
import com.hexagonkt.http.client.HttpClientSettings
import com.hexagonkt.http.model.ContentType
import com.hexagonkt.http.model.OK_200
import com.hexagonkt.http.server.async.HttpServer
import org.junit.jupiter.api.AfterAll
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestInstance
import org.junit.jupiter.api.TestInstance.Lifecycle.PER_CLASS
import java.net.URL
import kotlin.test.assertEquals

// hello_world
import com.hexagonkt.http.server.netty.async.serve

lateinit var server: HttpServer

/**
 * Start a Hello World server, serving at path "/hello".
 */
fun main() {
    server = serve {
        get("/hello/{name}") {
            val name = pathParameters["name"]
            ok("Hello $name!", contentType = ContentType(TEXT_PLAIN)).done()
        }
    }
}
// hello_world

@TestInstance(PER_CLASS)
internal class HelloWorldTest {

    private val client: HttpClient by lazy {
        val settings = HttpClientSettings(URL("http://localhost:${server.runtimePort}"))
        HttpClient(JettyClientAdapter(), settings)
    }

    @BeforeAll fun initialize() {
        main()
        client.start()
    }

    @AfterAll fun shutdown() {
        client.stop()
        server.stop()
    }

    @Test fun `A request returns 200 and the greeting test`() {
        val result = client.get("/hello/Ada")
        assertEquals("Hello Ada!", result.body)
        assertEquals(OK_200, result.status)
        assertEquals(ContentType(TEXT_PLAIN), result.contentType)
    }
}
