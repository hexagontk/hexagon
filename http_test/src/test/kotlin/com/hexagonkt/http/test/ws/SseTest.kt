package com.hexagonkt.http.test.ws

import com.hexagonkt.core.logging.LoggingLevel.DEBUG
import com.hexagonkt.core.logging.LoggingManager
import com.hexagonkt.core.println
import com.hexagonkt.http.model.ServerEvent
import com.hexagonkt.http.server.HttpServer
import com.hexagonkt.http.server.callbacks.UrlCallback
import com.hexagonkt.http.server.netty.NettyServerAdapter
import com.hexagonkt.http.server.serve
import com.hexagonkt.logging.slf4j.jul.Slf4jJulLoggingAdapter
import org.eclipse.jetty.http.HttpMethod.GET
import java.net.URI
import java.net.URL
import java.util.concurrent.SubmissionPublisher
import org.eclipse.jetty.client.HttpClient as JettyHttpClient

fun main () {
    LoggingManager.adapter = Slf4jJulLoggingAdapter()
    LoggingManager.setLoggerLevel("com.hexagonkt", DEBUG)

    val eventPublisher = SubmissionPublisher<ServerEvent>()

    val server: HttpServer = serve(NettyServerAdapter()) {
        get("/sse") { sse(eventPublisher) }
        get(callback = UrlCallback(URL("classpath:ws.html")))
    }

//    Desktop.getDesktop().browse(URI("http://localhost:${server.runtimePort}"))

    val items: List<ServerEvent> = listOf(
        ServerEvent(data = "d1"),
        ServerEvent(data = "d2"),
        ServerEvent(data = "d3"),
    )

    Thread.sleep(1_000)

    val jettyClient = JettyHttpClient()
    jettyClient.start()

    val r = jettyClient
        .newRequest(URI("http://localhost:${server.runtimePort}/sse"))
        .method(GET)

    r
        .onResponseBegin {
            it.status.println("RESP BEGIN ")
        }
        .onResponseContent { response, content ->
            println("CONTENT")
            while (content.hasRemaining())
                Char(content.get().toInt()).println()
        }
        .send { it.response.println("RESP COMPLETED ") }

    Thread.sleep(1_000)

    for (item in items) {
        println(item)
        eventPublisher.submit(item)
    }

    eventPublisher.close()
}
