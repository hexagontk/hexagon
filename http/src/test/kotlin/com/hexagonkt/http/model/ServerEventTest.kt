package com.hexagonkt.http.model

import org.junit.jupiter.api.Test
import kotlin.test.assertEquals

internal class ServerEventTest {

    @Test fun `Server events are formatted correctly`() {
        assertEquals(":\n\n", ServerEvent().eventData)
        assertEquals("event: event\n\n", ServerEvent(event = "event").eventData)
        assertEquals("data: data\n\n", ServerEvent(data = "data").eventData)
        assertEquals("id: id\n\n", ServerEvent(id = "id").eventData)
        assertEquals("retry: 100\n\n", ServerEvent(retry = 100).eventData)
        assertEquals(
            "event: event\ndata: data\nid: id\nretry: 100\n\n",
            ServerEvent(event = "event", data = "data", id = "id", retry = 100).eventData
        )
    }
}
