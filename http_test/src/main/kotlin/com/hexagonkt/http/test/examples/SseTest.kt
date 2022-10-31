package com.hexagonkt.http.test.examples

import com.hexagonkt.core.logging.info
import com.hexagonkt.http.client.HttpClientPort
import com.hexagonkt.http.server.handlers.PathHandler
import com.hexagonkt.http.server.handlers.path
import com.hexagonkt.http.model.ServerEvent
import com.hexagonkt.http.server.HttpServerPort
import com.hexagonkt.http.server.HttpServerSettings
import com.hexagonkt.http.server.handlers.HttpHandler
import com.hexagonkt.http.test.BaseTest
import org.junit.jupiter.api.Test
import java.util.concurrent.Flow
import java.util.concurrent.Flow.Subscription
import java.util.concurrent.SubmissionPublisher
import kotlin.concurrent.thread
import kotlin.test.assertEquals

@Suppress("FunctionName") // This class's functions are intended to be used only in tests
abstract class SseTest(
    final override val clientAdapter: () -> HttpClientPort,
    final override val serverAdapter: () -> HttpServerPort,
    final override val serverSettings: HttpServerSettings = HttpServerSettings(),
) : BaseTest() {

    // sse
    private val eventPublisher = SubmissionPublisher<ServerEvent>()

    private val path: PathHandler = path {
        get("/sse") {
            sse(eventPublisher)
        }
    }
    // sse

    override val handler: HttpHandler = path

    @Test fun `Request with invalid user returns 403`() {
        thread(true) { client.get("/sse") }

        var items: List<ServerEvent> = listOf(
            ServerEvent(data = "d1"),
            ServerEvent(data = "d2"),
            ServerEvent(data = "d3"),
        )

        // TODO The subscription must be done on HTTP response, this is testing nothing!
        eventPublisher.subscribe(object : Flow.Subscriber<ServerEvent> {
            override fun onComplete() {}

            override fun onError(throwable: Throwable) {}

            override fun onNext(item: ServerEvent) {
                val expectedItem = items[0]
                items = items.drop(1)
                assertEquals(expectedItem, item.info())
            }

            override fun onSubscribe(subscription: Subscription) {
                subscription.request(Long.MAX_VALUE)
            }
        })

        for (item in items)
            eventPublisher.submit(item)

        eventPublisher.close()
        Thread.sleep(50)
        assertEquals(0, items.size)
    }
}
