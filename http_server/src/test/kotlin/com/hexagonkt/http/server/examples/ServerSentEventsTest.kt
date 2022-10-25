package com.hexagonkt.http.server.examples

import com.hexagonkt.core.fail
import com.hexagonkt.core.logging.info
import com.hexagonkt.core.media.TextMedia
import com.hexagonkt.http.model.HttpMethod.GET
import com.hexagonkt.http.model.SuccessStatus.OK
import com.hexagonkt.http.server.handlers.PathHandler
import com.hexagonkt.http.server.handlers.path
import com.hexagonkt.http.model.HttpServerEvent
import org.junit.jupiter.api.Test
import java.util.concurrent.Flow
import java.util.concurrent.Flow.Subscription
import java.util.concurrent.SubmissionPublisher
import kotlin.test.assertEquals

internal class ServerSentEventsTest {

    private val eventPublisher = SubmissionPublisher<HttpServerEvent>()

    private val path: PathHandler = path {
        get("/sse") {
            sse(eventPublisher)
        }
    }

    @Suppress("UNCHECKED_CAST") // For testing purposes only
    @Test fun `Request with invalid user returns 403`() {
        val response = path.send(GET, "/sse")
        assertEquals(OK, response.status)
        assertEquals(TextMedia.EVENT_STREAM, response.contentType?.mediaType)
        assertEquals("no-cache", response.headers["cache-control"]?.value)

        val publisher = response.body as? SubmissionPublisher<HttpServerEvent> ?: fail
        val items: List<HttpServerEvent> = listOf(
            HttpServerEvent(data = "d1"),
            HttpServerEvent(data = "d2"),
            HttpServerEvent(data = "d3"),
        )
        var expectedItems: List<HttpServerEvent> = listOf(
            HttpServerEvent(data = "d1"),
            HttpServerEvent(data = "d2"),
            HttpServerEvent(data = "d3"),
        )

        publisher.subscribe(object : Flow.Subscriber<HttpServerEvent> {
            override fun onComplete() {}

            override fun onError(throwable: Throwable) {}

            override fun onNext(item: HttpServerEvent) {
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
