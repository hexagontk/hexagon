package com.hexagonkt.server.jetty

import com.hexagonkt.server.EngineTest
import org.testng.annotations.AfterClass
import org.testng.annotations.BeforeClass
import org.testng.annotations.Test

class JettyServletEngineAsyncTest : EngineTest(JettyServletEngine(true)) {
    @BeforeClass fun start () { startServers() }
    @AfterClass fun stop () { stopServers() }
    @Test fun validateEngine() { validate() }
}
