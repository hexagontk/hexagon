package com.hexagonkt.http.test.examples

import com.hexagonkt.core.fail
import com.hexagonkt.core.logging.info
import com.hexagonkt.core.media.TextMedia
import com.hexagonkt.http.client.HttpClientPort
import com.hexagonkt.http.model.SuccessStatus.OK
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

    @Suppress("UNCHECKED_CAST") // For testing purposes only
    @Test fun `Request with invalid user returns 403`() {
        val response = client.get("/sse")
        assertEquals(OK, response.status)
        assertEquals(TextMedia.EVENT_STREAM, response.contentType?.mediaType)
        assertEquals("no-cache", response.headers["cache-control"]?.value)

        val publisher = response.body as? SubmissionPublisher<ServerEvent> ?: fail
        val items: List<ServerEvent> = listOf(
            ServerEvent(data = "d1"),
            ServerEvent(data = "d2"),
            ServerEvent(data = "d3"),
        )
        var expectedItems: List<ServerEvent> = listOf(
            ServerEvent(data = "d1"),
            ServerEvent(data = "d2"),
            ServerEvent(data = "d3"),
        )

        publisher.subscribe(object : Flow.Subscriber<ServerEvent> {
            override fun onComplete() {}

            override fun onError(throwable: Throwable) {}

            override fun onNext(item: ServerEvent) {
                val expectedItem = expectedItems[0]
                expectedItems = expectedItems.drop(1)
                assertEquals(expectedItem, item.info())
            }

            override fun onSubscribe(subscription: Subscription) {}
        })

        for (item in items)
            publisher.submit(item)
    }
}
