package com.hexagonkt.server.jetty

import com.hexagonkt.server.EngineTest
import com.hexagonkt.server.Router
import com.hexagonkt.server.Server
import com.hexagonkt.templates.pebble.PebbleAdapter
import org.testng.annotations.AfterClass
import org.testng.annotations.BeforeClass
import org.testng.annotations.Test
import kotlin.test.assertFailsWith

class JettyServletAdapterTest : EngineTest(JettyServletAdapter(), PebbleAdapter) {
    @BeforeClass fun start () { startServers() }
    @AfterClass fun stop () { stopServers() }
    @Test fun validateEngine() { validate() }

    @Test fun start_test() {
        val engine = JettyServletAdapter()
        val message = "Jetty port uninitialized. Use lazy evaluation for HTTP client ;)"
        assert(!engine.started())
        assertFailsWith<IllegalStateException>(message) { engine.runtimePort() }
        engine.shutdown()
        assert(!engine.started())
    }

    @Test fun start_server() {
        val s = Server(JettyServletAdapter(), Router())
        s.run()
        assert(s.started())
        s.stop()
        assert(!s.started())
    }
}
