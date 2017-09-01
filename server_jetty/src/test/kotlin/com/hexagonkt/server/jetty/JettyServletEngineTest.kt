package com.hexagonkt.server.jetty

import com.hexagonkt.server.EngineTest
import com.hexagonkt.server.Server
import org.testng.annotations.AfterClass
import org.testng.annotations.BeforeClass
import org.testng.annotations.Test
import kotlin.test.assertFailsWith

class JettyServletEngineTest : EngineTest(JettyServletEngine()) {
    @BeforeClass fun start () { startServers() }
    @AfterClass fun stop () { stopServers() }
    @Test fun validateEngine() { validate() }

    @Test fun start_test() {
        val engine = JettyServletEngine()
        val message = "Jetty port uninitialized. Use lazy evaluation for HTTP client ;)"
        assert(!engine.started())
        assertFailsWith<IllegalStateException>(message) { engine.runtimePort() }
        engine.shutdown()
        assert(!engine.started())
    }

    @Test fun start_server() {
        val s: Server = server {}
        s.run()
        assert(s.started())
        s.stop()
        assert(!s.started())
    }

    @Test fun test_serve() {
        val s: Server = serve {}
        assert(s.started())
        s.stop()
        assert(!s.started())
    }
}
