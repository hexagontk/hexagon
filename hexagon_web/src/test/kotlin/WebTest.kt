package com.hexagonkt.web

import com.hexagonkt.http.client.Client
import com.hexagonkt.http.client.ahc.AhcAdapter
import com.hexagonkt.http.server.Router
import com.hexagonkt.http.server.Server
import com.hexagonkt.http.server.ServerSettings
import com.hexagonkt.http.server.jetty.JettyServletAdapter
import com.hexagonkt.templates.TemplateManager
import com.hexagonkt.templates.pebble.PebbleAdapter
import kotlinx.html.body
import kotlinx.html.p
import org.junit.jupiter.api.AfterAll
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestInstance
import org.junit.jupiter.api.TestInstance.Lifecycle.PER_CLASS
import java.time.LocalDateTime
import kotlin.test.assertEquals

@TestInstance(PER_CLASS)
internal class WebTest {

    private val templateEngine = PebbleAdapter

    private val router: Router = Router {
        get("/template") {
            attributes += "date" to LocalDateTime.now()
            template("templates/pebble_template.html")
        }

        get("/template/adapter") {
            attributes += "date" to LocalDateTime.now()
            template(templateEngine, "templates/pebble_template.html")
        }

        get("/html") {
            html {
                body {
                    p { +"Hello HTML DSL" }
                }
            }
        }
    }

    private val server: Server = Server(JettyServletAdapter(), router, ServerSettings(bindPort = 0))

    private val client by lazy { Client(AhcAdapter(), "http://localhost:${server.runtimePort}") }

    @BeforeAll fun start() {
        TemplateManager.adapters = mapOf(".*\\.html".toRegex() to PebbleAdapter)
        server.start()
    }

    @AfterAll fun stop() {
        server.stop()
    }

    @Test fun template() {
        val response = client.get("/template")
        assertEquals(200, response.status)
    }

    @Test fun templateAdapter() {
        val response = client.get("/template/adapter")
        assertEquals(200, response.status)
    }

    @Test fun html() {
        val response = client.get("/html")
        assert(response.headers["Content-Type"]?.first() == "text/html")
        assertEquals(200, response.status)
        assert(response.body?.contains("<p>Hello HTML DSL</p>") ?: false)
    }
}
