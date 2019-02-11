package com.hexagonkt.web

import com.hexagonkt.http.client.Client
import com.hexagonkt.http.server.Router
import com.hexagonkt.http.server.Server
import com.hexagonkt.http.server.jetty.JettyServletAdapter
import com.hexagonkt.settings.SettingsManager
import com.hexagonkt.templates.pebble.PebbleAdapter
import org.testng.annotations.AfterTest
import org.testng.annotations.BeforeTest
import org.testng.annotations.Test
import java.time.LocalDateTime
import java.util.Locale.getDefault as defaultLocale

@Test class WebTest {

    private val router: Router = Router {
        get("/template") {
            attributes += "date" to LocalDateTime.now()
            template(PebbleAdapter, "pebble_template.html")
        }
    }

    private val server: Server = Server(JettyServletAdapter(), router, SettingsManager.settings)

    private val client by lazy { Client("http://localhost:${server.runtimePort}") }

    @BeforeTest fun start() {
        server.run()
    }

    @AfterTest fun stop() {
        server.stop()
    }

    @Test fun template() {
        val response = client.get("/template")
        assert(response.statusCode == 200)
    }
}

