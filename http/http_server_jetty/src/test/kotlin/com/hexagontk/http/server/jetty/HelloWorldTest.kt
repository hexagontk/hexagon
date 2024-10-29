@file:Suppress("UnusedImport") // Unused import left for the sake of documentation completeness

package com.hexagontk.http.server.jetty

import com.hexagontk.http.client.jetty.JettyHttpClient
import com.hexagontk.http.client.HttpClient
import com.hexagontk.http.client.HttpClientSettings
import com.hexagontk.http.model.OK_200
import org.junit.jupiter.api.AfterAll
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestInstance
import org.junit.jupiter.api.TestInstance.Lifecycle.PER_CLASS
import kotlin.test.assertEquals
import com.hexagontk.core.urlOf

// hello_world
import com.hexagontk.core.media.TEXT_PLAIN
import com.hexagontk.http.model.ContentType
import com.hexagontk.http.server.HttpServer
import com.hexagontk.http.server.jetty.serve

lateinit var server: HttpServer

/**
 * Start a Hello World server, serving at path "/hello".
 */
fun main() {
    server = serve {
        get("/hello/{name}") {
            val name = pathParameters["name"]
            ok("Hello $name!", contentType = ContentType(TEXT_PLAIN))
        }
    }
}
// hello_world

@TestInstance(PER_CLASS)
internal class HelloWorldTest {

    private val client: HttpClient by lazy {
        val settings = HttpClientSettings(urlOf("http://localhost:${server.runtimePort}"))
        HttpClient(JettyHttpClient(), settings)
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
