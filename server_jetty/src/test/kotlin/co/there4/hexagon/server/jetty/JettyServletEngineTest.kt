package co.there4.hexagon.server.jetty

import co.there4.hexagon.server.EngineTest
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
}
