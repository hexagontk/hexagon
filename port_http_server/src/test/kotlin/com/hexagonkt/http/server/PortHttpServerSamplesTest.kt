package com.hexagonkt.http.server

import com.hexagonkt.injection.InjectionManager
import org.testng.annotations.Test
import java.net.InetAddress

@Test abstract class PortHttpServerSamplesTest(val adapter: ServerPort) {

    @Test fun serverCreation() {
        // serverCreation
        val customServer = Server(adapter, Router(), "name", InetAddress.getByName("0.0.0"), 2020)

        customServer.start()
        assert(customServer.started())
        customServer.stop()

        InjectionManager.bindObject(adapter)
        val defaultServer = Server {}

        defaultServer.start()
        assert(defaultServer.started())
        defaultServer.stop()
        // serverCreation
    }
}
