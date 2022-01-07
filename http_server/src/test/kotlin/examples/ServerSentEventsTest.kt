package com.hexagonkt.http.server.examples

import com.hexagonkt.core.helpers.fail
import com.hexagonkt.core.logging.info
import com.hexagonkt.core.media.TextMedia
import com.hexagonkt.http.model.HttpMethod.GET
import com.hexagonkt.http.model.SuccessStatus.OK
import com.hexagonkt.http.server.handlers.PathHandler
import com.hexagonkt.http.server.handlers.path
import com.hexagonkt.http.model.HttpServerEvent
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.runBlocking
import org.junit.jupiter.api.Test
import kotlin.test.assertEquals

internal class ServerSentEventsTest {

    // sse
    private val path: PathHandler = path {
        get("/sse") {
            sse(flow {
                emit(HttpServerEvent())
                emit(HttpServerEvent())
            })
        }
    }
    // sse

    @Test fun `Request with invalid user returns 403`() = runBlocking<Unit> {
        val response = path.send(GET, "/sse")
        assertEquals(OK, response.status)
        assertEquals(TextMedia.EVENT_STREAM, response.contentType?.mediaType)
        assertEquals("no-cache", response.headers["cache-control"])
        val flow = response.body as? Flow<*> ?: fail
        flow.onEach {
            assertEquals(HttpServerEvent(), it.info())
        }
    }
}
