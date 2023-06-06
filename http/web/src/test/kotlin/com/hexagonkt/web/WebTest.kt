package com.hexagonkt.web

import com.hexagonkt.http.client.HttpClient
import com.hexagonkt.http.client.HttpClientSettings
import com.hexagonkt.http.client.jetty.JettyClientAdapter
import com.hexagonkt.http.model.OK_200
import com.hexagonkt.http.server.HttpServer
import com.hexagonkt.http.server.HttpServerSettings
import com.hexagonkt.http.handlers.PathHandler
import com.hexagonkt.http.handlers.path
import com.hexagonkt.http.server.jetty.JettyServletAdapter
import com.hexagonkt.templates.TemplateManager
import com.hexagonkt.templates.pebble.PebbleAdapter
import org.junit.jupiter.api.AfterAll
import org.junit.jupiter.api.BeforeAll
import kotlin.test.Test
import org.junit.jupiter.api.TestInstance
import org.junit.jupiter.api.TestInstance.Lifecycle.PER_CLASS
import java.net.URL
import java.time.LocalDateTime
import kotlin.test.assertContains
import kotlin.test.assertEquals

@TestInstance(PER_CLASS)
internal class WebTest {

    private val templateEngine = PebbleAdapter()

    private val router: PathHandler = path {
        get("/template") {
            template(
                url = URL("classpath:templates/pebble_template.html"),
                context = callContext() + ("date" to LocalDateTime.now())
            )
        }

        get("/template/adapter") {
            template(
                templateEngine = templateEngine,
                url = URL("classpath:templates/pebble_template.html"),
                context = callContext() + ("date" to LocalDateTime.now())
            )
        }
    }

    private val server: HttpServer by lazy {
        HttpServer(JettyServletAdapter(), router, HttpServerSettings(bindPort = 0))
    }

    private val client by lazy {
        val settings = HttpClientSettings(URL("http://localhost:${server.runtimePort}"))
        HttpClient(JettyClientAdapter(), settings)
    }

    @BeforeAll fun start() {
        TemplateManager.adapters = mapOf(".*\\.html".toRegex() to PebbleAdapter())
        server.start()
        client.start()
    }

    @AfterAll fun stop() {
        server.stop()
        client.stop()
    }

    @Test fun template() {
        val response = client.get("/template")
        assertEquals(OK_200, response.status)
        assertContains(response.bodyString(), "<p>path : /template</p>")
    }

    @Test fun templateAdapter() {
        val response = client.get("/template/adapter")
        assertEquals(OK_200, response.status)
    }
}
