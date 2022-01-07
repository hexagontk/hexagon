package com.hexagonkt.web

import com.hexagonkt.http.client.HttpClient
import com.hexagonkt.http.client.jetty.JettyClientAdapter
import com.hexagonkt.http.model.SuccessStatus.OK
import com.hexagonkt.http.server.HttpServer
import com.hexagonkt.http.server.HttpServerSettings
import com.hexagonkt.http.server.handlers.PathHandler
import com.hexagonkt.http.server.handlers.path
import com.hexagonkt.http.server.jetty.JettyServletAdapter
import com.hexagonkt.templates.TemplateManager
import com.hexagonkt.templates.pebble.PebbleAdapter
import kotlinx.coroutines.runBlocking
import kotlinx.html.body
import kotlinx.html.p
import org.junit.jupiter.api.AfterAll
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestInstance
import org.junit.jupiter.api.TestInstance.Lifecycle.PER_CLASS
import java.net.URL
import java.time.LocalDateTime
import kotlin.test.assertEquals

@TestInstance(PER_CLASS)
internal class WebTest {

    private val templateEngine = PebbleAdapter

    private val router: PathHandler = path {
        get("/template") {
            template(
                url = URL("classpath:templates/pebble_template.html"),
                context = fullContext() + ("date" to LocalDateTime.now())
            )
        }

        get("/template/adapter") {
            template(
                templateEngine = templateEngine,
                url = URL("classpath:templates/pebble_template.html"),
                context = fullContext() + ("date" to LocalDateTime.now())
            )
        }

        get("/html") {
            html {
                body {
                    p { +"Hello HTML DSL" }
                }
            }
        }
    }

    private val server: HttpServer by lazy {
        HttpServer(
            JettyServletAdapter(),
            listOf(router),
            HttpServerSettings(bindPort = 0)
        )
    }

    private val client by lazy {
        HttpClient(JettyClientAdapter(), URL("http://localhost:${server.runtimePort}"))
    }

    @BeforeAll fun start() {
        TemplateManager.adapters = mapOf(".*\\.html".toRegex() to PebbleAdapter)
        server.start()
        client.start()
    }

    @AfterAll fun stop() {
        server.stop()
        client.stop()
    }

    @Test fun template() = runBlocking {
        val response = client.get("/template")
        assertEquals(OK, response.status)
    }

    @Test fun templateAdapter() = runBlocking {
        val response = client.get("/template/adapter")
        assertEquals(OK, response.status)
    }

    @Test fun html() = runBlocking {
        val response = client.get("/html")
        assertEquals("text/html", response.contentType?.mediaType?.fullType)
        assertEquals(OK, response.status)
        assert(response.bodyString().contains("<p>Hello HTML DSL</p>"))
    }
}
