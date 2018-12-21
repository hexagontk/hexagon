package com.hexagonkt.http.server.jetty

import com.hexagonkt.http.server.EngineTest
import org.testng.annotations.AfterClass
import org.testng.annotations.BeforeClass
import org.testng.annotations.Test

class JettyServletAdapterAsyncTest : EngineTest(JettyServletAdapter(true)) {
    @BeforeClass fun start () { startServers() }
    @AfterClass fun stop () { stopServers() }
    @Test fun validateEngine() { validate() }
}
