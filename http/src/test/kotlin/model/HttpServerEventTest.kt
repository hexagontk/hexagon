package com.hexagonkt.http.model

import org.junit.jupiter.api.Test
import kotlin.test.assertEquals

internal class HttpServerEventTest {

    @Test fun `Server events are formatted correctly`() {
        assertEquals(":\n\n", HttpServerEvent().eventData)
        assertEquals("event: event\n\n", HttpServerEvent(event = "event").eventData)
        assertEquals("data: data\n\n", HttpServerEvent(data = "data").eventData)
        assertEquals("id: id\n\n", HttpServerEvent(id = "id").eventData)
        assertEquals("retry: 100\n\n", HttpServerEvent(retry = 100).eventData)
        assertEquals(
            "event: event\ndata: data\nid: id\nretry: 100\n\n",
            HttpServerEvent(event = "event", data = "data", id = "id", retry = 100).eventData
        )
    }
}
