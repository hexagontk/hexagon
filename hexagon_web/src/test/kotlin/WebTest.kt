package com.hexagonkt.web

import com.hexagonkt.http.client.Client
import com.hexagonkt.http.client.ahc.AhcAdapter
import com.hexagonkt.http.server.Router
import com.hexagonkt.http.server.Server
import com.hexagonkt.http.server.ServerSettings
import com.hexagonkt.http.server.jetty.JettyServletAdapter
import com.hexagonkt.templates.TemplateEngine
import com.hexagonkt.templates.TemplateEngineSettings
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

@TestInstance(PER_CLASS)
internal class WebTest {

    private val templateEngine =
        TemplateEngine(PebbleAdapter, TemplateEngineSettings(basePath = "templates"))

    private val router: Router = Router {
        get("/template") {
            attributes += "date" to LocalDateTime.now()
            template(templateEngine, "pebble_template.html")
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
        TemplateManager.register("prefix", TemplateEngine(PebbleAdapter))
        server.start()
    }

    @AfterAll fun stop() {
        server.stop()
    }

    @Test fun template() {
        val response = client.get("/template")
        assert(response.status == 200)
    }

    @Test fun html() {
        val response = client.get("/html")
        assert(response.headers["Content-Type"]?.first() == "text/html")
        assert(response.status == 200)
        assert(response.body?.contains("<p>Hello HTML DSL</p>") ?: false)
    }
}
