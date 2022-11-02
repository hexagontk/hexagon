package com.hexagonkt.http.model.ws

import com.hexagonkt.http.model.ws.CloseStatus.BAD_DATA
import com.hexagonkt.http.model.ws.CloseStatus.NORMAL
import org.junit.jupiter.api.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull

internal class WebSocketModelTest {

    @Test fun `Cover WebSockets close status values`() {
        assertEquals(1000, NORMAL.code)
        assertEquals(1001, CloseStatus.SHUTDOWN.code)
        assertEquals(1002, CloseStatus.PROTOCOL.code)
        assertEquals(1003, BAD_DATA.code)
        assertEquals(1004, CloseStatus.UNDEFINED.code)
        assertEquals(1005, CloseStatus.NO_CODE.code)
        assertEquals(1006, CloseStatus.NO_CLOSE.code)
        assertEquals(CloseStatus.NO_CLOSE.code, CloseStatus.ABNORMAL.code)
        assertEquals(1007, CloseStatus.BAD_PAYLOAD.code)
        assertEquals(1008, CloseStatus.POLICY_VIOLATION.code)
        assertEquals(1009, CloseStatus.MESSAGE_TOO_LARGE.code)
        assertEquals(1010, CloseStatus.REQUIRED_EXTENSION.code)
        assertEquals(1011, CloseStatus.SERVER_ERROR.code)
        assertEquals(1012, CloseStatus.SERVICE_RESTART.code)
        assertEquals(1013, CloseStatus.TRY_AGAIN_LATER.code)
        assertEquals(1014, CloseStatus.INVALID_UPSTREAM_RESPONSE.code)
        assertEquals(1015, CloseStatus.FAILED_TLS_HANDSHAKE.code)
    }

    @Test fun `Custom WebSocket close statuses can be created`() {
        assertEquals(65_321, CustomCloseStatus(65_321).code)
    }

    @Test fun `Default session close parameters`() {
        open class TestSession(
            val expectedStatus: WsCloseStatus,
            val expectedReason: String,
        ) : WsSession {
            override fun send(data: ByteArray) {}
            override fun send(text: String) {}
            override fun ping(data: ByteArray) {}
            override fun pong(data: ByteArray) {}
            override fun close(status: WsCloseStatus, reason: String) {
                assertEquals(expectedStatus, status)
                assertEquals(expectedReason, reason)
            }
        }

        TestSession(NORMAL, "").close()
        TestSession(BAD_DATA, "").close(BAD_DATA)
        TestSession(NORMAL, "reason").close(reason = "reason")
        TestSession(BAD_DATA, "reason").close(BAD_DATA, "reason")
    }

    @Test fun `Close status can be fetched from code`() {
        assertNull(CloseStatus[2000])
        assertNull(CloseStatus.valueOfOrNull(2000))
        assertEquals(NORMAL, CloseStatus[1000])
        assertEquals(NORMAL, CloseStatus.valueOfOrNull(1000))
        assertEquals(NORMAL, CloseStatus.valueOf(1000))
        assertEquals(CustomCloseStatus(2000), CloseStatus.valueOf(2000))
    }
}
