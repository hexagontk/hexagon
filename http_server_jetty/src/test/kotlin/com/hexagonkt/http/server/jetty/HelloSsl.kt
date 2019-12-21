package com.hexagonkt.http.server.jetty

import com.hexagonkt.http.Protocol
import com.hexagonkt.http.client.Client
import com.hexagonkt.http.server.ServerPort
import com.hexagonkt.http.server.ServerSettings
import com.hexagonkt.http.server.SslSettings
import com.hexagonkt.http.server.serve
import com.hexagonkt.injection.InjectionManager

fun main() {
    // Make this JVM trust CAs in `ca.p12` (to keep JRE ones, use `cacerts` as base, otherwise
    // standard ones won't be trusted (I.e.: Let's Encrypt or Google)
    System.setProperty("javax.net.ssl.trustStore", "src/test/resources/ssl/ca.p12")
    System.setProperty("javax.net.ssl.trustStoreType", "pkcs12")
    System.setProperty("javax.net.ssl.trustStorePassword", "21p.ac")
    InjectionManager.bindObject<ServerPort>(JettyServletAdapter())

    val settings = ServerSettings(
        protocol = Protocol.HTTP2,
        sslSettings = SslSettings(
            keyStorePassword = "hexagonkt_store.p12".reversed(),
            trustStorePassword = "trust_store.p12".reversed()
        )
    )

    val server = serve(settings) {
        get("/hello") {
            ok("Hello World!")
        }
    }

    // Supply finger prints to trust a set of servers (but not others, even signed with same CA)
//    val fingerprints = arrayOf("3A:1A:55:99:0D:7D:E4:A1:EC:90:91:1D:E3:52:F0:D5:6A:70:EC:65")
//    val cf = Client("https://localhost:${server.runtimePort}", fingerprints = *fingerprints)
    val c = Client("https://localhost:${server.runtimePort}")
    val r = c.get("/hello")
    println(r.responseBody)
}
