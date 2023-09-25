package com.hexagonkt.http.model.ws

import com.hexagonkt.core.fail
import com.hexagonkt.http.model.HttpRequestPort
import java.net.URI
import org.junit.jupiter.api.Test
import kotlin.test.assertEquals

internal class WebSocketModelTest {

    @Test fun `Cover WebSockets close status values`() {
        assertEquals(1000, NORMAL)
        assertEquals(1001, SHUTDOWN)
        assertEquals(1002, PROTOCOL)
        assertEquals(1003, BAD_DATA)
        assertEquals(1004, UNDEFINED)
        assertEquals(1005, NO_CODE)
        assertEquals(1006, NO_CLOSE)
        assertEquals(NO_CLOSE, ABNORMAL)
        assertEquals(1007, BAD_PAYLOAD)
        assertEquals(1008, POLICY_VIOLATION)
        assertEquals(1009, MESSAGE_TOO_LARGE)
        assertEquals(1010, REQUIRED_EXTENSION)
        assertEquals(1011, SERVER_ERROR)
        assertEquals(1012, SERVICE_RESTART)
        assertEquals(1013, TRY_AGAIN_LATER)
        assertEquals(1014, INVALID_UPSTREAM_RESPONSE)
        assertEquals(1015, FAILED_TLS_HANDSHAKE)
    }

    @Test fun `Default session close parameters`() {
        open class TestSession(
            val expectedStatus: Int,
            val expectedReason: String,
        ) : WsSession {
            override val uri: URI get() = fail
            override val attributes: Map<*, *> get() = fail
            override val request: HttpRequestPort get() = fail
            override val exception: Exception? get() = fail
            override val pathParameters: Map<String, String> get() = fail

            override fun send(data: ByteArray) {}
            override fun send(text: String) {}
            override fun ping(data: ByteArray) {}
            override fun pong(data: ByteArray) {}
            override fun close(status: Int, reason: String) {
                assertEquals(expectedStatus, status)
                assertEquals(expectedReason, reason)
            }
        }

        TestSession(NORMAL, "").close()
        TestSession(BAD_DATA, "").close(BAD_DATA)
        TestSession(NORMAL, "reason").close(reason = "reason")
        TestSession(BAD_DATA, "reason").close(BAD_DATA, "reason")
    }
}
