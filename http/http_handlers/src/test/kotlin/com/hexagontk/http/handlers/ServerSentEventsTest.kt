package com.hexagontk.http.handlers

import com.hexagontk.core.fail
import com.hexagontk.core.info
import com.hexagontk.core.media.TEXT_EVENT_STREAM
import com.hexagontk.http.model.HttpMethod.GET
import com.hexagontk.http.model.OK_200
import com.hexagontk.http.model.ServerEvent
import org.junit.jupiter.api.Test
import java.util.concurrent.Flow
import java.util.concurrent.Flow.Subscription
import java.util.concurrent.SubmissionPublisher
import kotlin.test.assertEquals

internal class ServerSentEventsTest {

    private val eventPublisher = SubmissionPublisher<ServerEvent>()

    private val path: PathHandler = path {
        get("/sse") {
            sse(eventPublisher)
        }
    }

    @Suppress("UNCHECKED_CAST") // For testing purposes only
    @Test fun `Request with invalid user returns 403`() {
        val response = path.send(GET, "/sse")
        assertEquals(OK_200, response.status)
        assertEquals(TEXT_EVENT_STREAM, response.contentType?.mediaType)
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
