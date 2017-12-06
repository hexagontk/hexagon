package com.hexagonkt.server.undertow

import com.hexagonkt.server.EngineTest
import com.hexagonkt.server.Server
import com.hexagonkt.templates.pebble.PebbleAdapter
import org.testng.annotations.AfterClass
import org.testng.annotations.BeforeClass
import org.testng.annotations.Test
import kotlin.test.assertFailsWith

class UndertowAdapterTest : EngineTest(UndertowAdapter(), PebbleAdapter) {
    @BeforeClass fun start () { startServers() }
    @AfterClass fun stop () { stopServers() }
    @Test fun validateEngine() { validate() }

    @Test fun `start test`() {
        val engine = UndertowAdapter()
        val message = "Undertow port uninitialized. Use lazy evaluation for HTTP client ;)"
        assert(!engine.started())
        assertFailsWith<IllegalStateException>(message) { engine.runtimePort() }
        engine.shutdown()
        assert(!engine.started())
    }

    @Test fun `start server`() {
        val s: Server = server {}
        s.run()
        assert(s.started())
        s.stop()
        assert(!s.started())
    }

    @Test fun `test serve`() {
        val s: Server = serve {}
        assert(s.started())
        s.stop()
        assert(!s.started())
    }
}
