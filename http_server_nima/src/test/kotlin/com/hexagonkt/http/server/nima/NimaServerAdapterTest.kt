package com.hexagonkt.http.server.nima

import com.hexagonkt.http.server.HttpServer
import org.junit.jupiter.api.Test

internal class NimaServerAdapterTest {

    @Test fun `Nima starts`() {
        main()
        server.stop()
    }
}

lateinit var server: HttpServer

fun main() {
    server = serve {
        get("/ey") { ok("Done!") }
    }
}
