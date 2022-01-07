package com.hexagonkt.http.test

import com.hexagonkt.core.media.TextMedia.PLAIN
import com.hexagonkt.http.client.jetty.JettyClientAdapter
import com.hexagonkt.http.client.HttpClient
import com.hexagonkt.http.model.ContentType
import com.hexagonkt.http.model.SuccessStatus.OK
import com.hexagonkt.http.server.HttpServer
import com.hexagonkt.http.server.jetty.serve
import kotlinx.coroutines.runBlocking
import org.junit.jupiter.api.AfterAll
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestInstance
import org.junit.jupiter.api.TestInstance.Lifecycle.PER_CLASS
import java.net.URL
import kotlin.test.assertEquals

@TestInstance(PER_CLASS)
internal class HelloWorldTest {

    private val client: HttpClient by lazy {
        HttpClient(JettyClientAdapter(), URL("http://localhost:${server.runtimePort}"))
    }

    @BeforeAll fun initialize() {
        main()
        client.start()
    }

    @AfterAll fun shutdown() {
        client.stop()
        server.stop()
    }

    @Test fun `A request returns 200 and the greeting test`() = runBlocking {
        val result = client.get("/hello/Ada")
        assertEquals("Hello Ada!", result.body)
        assertEquals(OK, result.status)
        assertEquals(ContentType(PLAIN), result.contentType)
    }
}

// hello_world
lateinit var server: HttpServer

/**
 * Start a Hello World server, serving at path "/hello".
 */
fun main() {
    server = serve {
        get("/hello/{name}") {
            val name = pathParameters["name"]
            ok("Hello $name!", contentType = ContentType(PLAIN))
        }
    }
}
// hello_world
