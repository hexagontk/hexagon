package com.hexagonkt.http.server.jetty

import com.hexagonkt.http.client.Client
import com.hexagonkt.http.server.ServerPort
import com.hexagonkt.http.server.ServerSettings
import com.hexagonkt.http.server.SslSettings
import com.hexagonkt.http.server.serve
import com.hexagonkt.injection.InjectionManager

fun main() {
    System.setProperty("javax.net.ssl.trustStore", "src/test/resources/ssl/ca.p12")
    System.setProperty("javax.net.ssl.trustStoreType", "pkcs12")
    System.setProperty("javax.net.ssl.trustStorePassword", "hexagon")
    InjectionManager.bindObject<ServerPort>(JettyServletAdapter())

    val settings = ServerSettings(
        sslSettings = SslSettings(
            keyStorePassword = "hexagon",
            trustStorePassword = "hexagon"
        )
    )

    val server = serve(settings) {
        get("/hello") {
            ok("Hello World!")
        }
    }

//    val c = Client("https://localhost:${server.runtimePort}", insecure = true)
//    val c = Client("https://localhost:${server.runtimePort}", fingerprints = *arrayOf("3A:1A:55:99:0D:7D:E4:A1:EC:90:91:1D:E3:52:F0:D5:6A:70:EC:65"))
    val c = Client("https://localhost:${server.runtimePort}")
    val r = c.get("/hello")
    println(r.responseBody)
//    val g = Client("https://google.com")
//    val r2 = g.get("/")
//    println(r2.responseBody)
}
