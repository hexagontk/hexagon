package com.hexagontk.web

import com.hexagontk.core.urlOf
import com.hexagontk.http.client.HttpClient
import com.hexagontk.http.client.HttpClientSettings
import com.hexagontk.http.client.jetty.JettyHttpClient
import com.hexagontk.http.model.OK_200
import com.hexagontk.http.server.HttpServer
import com.hexagontk.http.server.HttpServerSettings
import com.hexagontk.http.handlers.PathHandler
import com.hexagontk.http.handlers.path
import com.hexagontk.http.server.jetty.JettyServletHttpServer
import com.hexagontk.templates.TemplateManager
import com.hexagontk.templates.pebble.Pebble
import org.junit.jupiter.api.AfterAll
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestInstance
import org.junit.jupiter.api.TestInstance.Lifecycle.PER_CLASS
import java.time.LocalDateTime
import kotlin.test.assertContains
import kotlin.test.assertEquals

@TestInstance(PER_CLASS)
internal class WebTest {

    private val templateEngine = Pebble()

    private val router: PathHandler = path {
        get("/template") {
            template(
                url = urlOf("classpath:templates/pebble_template.html"),
                context = callContext() + ("date" to LocalDateTime.now())
            )
        }

        get("/template/adapter") {
            template(
                templateEngine = templateEngine,
                url = urlOf("classpath:templates/pebble_template.html"),
                context = callContext() + ("date" to LocalDateTime.now())
            )
        }
    }

    private val server: HttpServer by lazy {
        HttpServer(JettyServletHttpServer(), router, HttpServerSettings(bindPort = 0))
    }

    private val client by lazy {
        val settings = HttpClientSettings(urlOf("http://localhost:${server.runtimePort}"))
        HttpClient(JettyHttpClient(), settings)
    }

    @BeforeAll fun start() {
        TemplateManager.adapters = mapOf(".*\\.html".toRegex() to Pebble())
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
