package com.hexagonkt.web

import com.hexagonkt.http.client.Client
import com.hexagonkt.http.client.ahc.AhcAdapter
import com.hexagonkt.http.server.Router
import com.hexagonkt.http.server.Server
import com.hexagonkt.http.server.jetty.JettyServletAdapter
import com.hexagonkt.settings.SettingsManager
import com.hexagonkt.templates.pebble.PebbleAdapter
import org.junit.jupiter.api.AfterAll
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestInstance
import org.junit.jupiter.api.TestInstance.Lifecycle.PER_CLASS
import java.time.LocalDateTime

@TestInstance(PER_CLASS)
class WebTest {

    private val router: Router = Router {
        get("/template") {
            attributes += "date" to LocalDateTime.now()
            template(PebbleAdapter, "pebble_template.html")
        }
    }

    private val server: Server = Server(JettyServletAdapter(), router, SettingsManager.settings)

    private val client by lazy { Client(AhcAdapter(), "http://localhost:${server.runtimePort}") }

    @BeforeAll fun start() {
        server.start()
    }

    @AfterAll fun stop() {
        server.stop()
    }

    @Test fun template() {
        val response = client.get("/template")
        assert(response.status == 200)
    }
}

