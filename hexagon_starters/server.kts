#!/bin/env kscript

/*
 * Due to a Kscript bug, this example runs only over Java 8
 */

@file:CompilerOpts("-jvm-target 1.8")
@file:MavenRepository("jcenter", "https://jcenter.bintray.com")
@file:DependsOn("com.hexagonkt:http_server_jetty:1.2.2")

import com.hexagonkt.http.server.Server
import com.hexagonkt.http.server.ServerPort
import com.hexagonkt.http.server.jetty.JettyServletAdapter
import com.hexagonkt.injection.InjectionManager

InjectionManager.bindObject<ServerPort>(JettyServletAdapter())

val server: Server = Server {
    get("/hello/{name}") { ok("Hello, ${pathParameters["name"]}!", "text/plain") }
}

server.start()
