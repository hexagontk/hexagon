package com.hexagonkt.server.jetty

import com.hexagonkt.server.EngineTest
import com.hexagonkt.templates.pebble.PebbleAdapter
import org.testng.annotations.AfterClass
import org.testng.annotations.BeforeClass
import org.testng.annotations.Test

class JettyServletAdapterAsyncTest : EngineTest(JettyServletAdapter(true), PebbleAdapter) {
    @BeforeClass fun start () { startServers() }
    @AfterClass fun stop () { stopServers() }
    @Test fun validateEngine() { validate() }
}
