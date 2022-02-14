package com.hexagonkt.http.test.examples

import com.hexagonkt.core.fail
import com.hexagonkt.core.media.TextMedia
import com.hexagonkt.http.client.HttpClientPort
import com.hexagonkt.http.model.SuccessStatus.OK
import com.hexagonkt.http.server.handlers.PathHandler
import com.hexagonkt.http.server.handlers.path
import com.hexagonkt.http.model.HttpServerEvent
import com.hexagonkt.http.server.HttpServerPort
import com.hexagonkt.http.server.HttpServerSettings
import com.hexagonkt.http.server.handlers.ServerHandler
import com.hexagonkt.http.test.BaseTest
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.runBlocking
import org.junit.jupiter.api.Test
import kotlin.test.assertEquals

@Suppress("FunctionName") // This class's functions are intended to be used only in tests
abstract class SseTest(
    final override val clientAdapter: () -> HttpClientPort,
    final override val serverAdapter: () -> HttpServerPort,
    final override val serverSettings: HttpServerSettings = HttpServerSettings(),
) : BaseTest() {

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

    override val handler: ServerHandler = path

    @Test fun `Request with invalid user returns 403`() = runBlocking<Unit> {
        val response = client.get("/sse")
        assertEquals(OK, response.status)
        assertEquals(TextMedia.EVENT_STREAM, response.contentType?.mediaType)
        assertEquals("no-cache", response.headers["cache-control"])
        val flow = response.body as? Flow<*> ?: fail
        flow.onEach {
            assertEquals(HttpServerEvent(), it)
        }
    }
}
