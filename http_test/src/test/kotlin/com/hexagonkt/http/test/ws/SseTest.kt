package com.hexagonkt.http.test.ws

import com.hexagonkt.core.logging.LoggingLevel.DEBUG
import com.hexagonkt.core.logging.LoggingManager
import com.hexagonkt.core.logging.info
import com.hexagonkt.http.model.ServerEvent
import com.hexagonkt.http.server.callbacks.UrlCallback
import com.hexagonkt.http.server.netty.NettyServerAdapter
import com.hexagonkt.http.server.serve
import com.hexagonkt.logging.slf4j.jul.Slf4jJulLoggingAdapter
import org.eclipse.jetty.http.HttpMethod.GET
import java.lang.StringBuilder
import java.net.URI
import java.net.URL
import java.util.concurrent.Flow
import java.util.concurrent.Flow.Subscription
import java.util.concurrent.SubmissionPublisher
import kotlin.test.assertEquals
import org.eclipse.jetty.client.HttpClient as JettyHttpClient

fun main () {
    LoggingManager.adapter = Slf4jJulLoggingAdapter()
    LoggingManager.setLoggerLevel("com.hexagonkt", DEBUG)

    val serverPublisher = SubmissionPublisher<ServerEvent>()
    val server = serve(NettyServerAdapter()) {
        get("/sse") { sse(serverPublisher) }
        get(callback = UrlCallback(URL("classpath:ws.html")))
    }

    var events: List<ServerEvent> = listOf(
        ServerEvent(event = "event", data = "d1"),
        ServerEvent(id = "id", data = "d2"),
        ServerEvent(retry = 10, data = "d3"),
    )

    val jettyClient = JettyHttpClient()
    jettyClient.start()

    val clientPublisher = SubmissionPublisher<ServerEvent>()
    jettyClient
        .newRequest(URI("http://localhost:${server.runtimePort}/sse"))
        .method(GET)
        .onResponseBegin {
            if (it.status !in 200 until 300)
                error("Invalid response: ${it.status}")
        }
        .onResponseContent { _, content ->
            val sb = StringBuilder()
            while (content.hasRemaining())
                sb.append(Char(content.get().toInt()))

            val evt = sb
                .trim()
                .lines()
                .map { it.split(":") }
                .associate { it.first().trim().lowercase() to it.last().trim() }
                .let { ServerEvent(it["event"], it["data"], it["id"], it["retry"]?.toLong()) }

            clientPublisher.submit(evt)
        }
        .send {}

    clientPublisher.subscribe(object : Flow.Subscriber<ServerEvent> {
        override fun onComplete() {}

        override fun onError(throwable: Throwable) {}

        override fun onNext(item: ServerEvent) {
            val expectedItem = events[0]
            events = events.drop(1)
            assertEquals(expectedItem, item.info())
        }

        override fun onSubscribe(subscription: Subscription) {
            subscription.request(Long.MAX_VALUE)
        }
    })

    Thread.sleep(50)
    for (item in events) {
        println(item)
        serverPublisher.submit(item)
    }

    Thread.sleep(50)
    serverPublisher.close()
    jettyClient.stop()
    server.stop()
}
