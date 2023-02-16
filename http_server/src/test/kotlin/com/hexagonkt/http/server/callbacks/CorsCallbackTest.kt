package com.hexagonkt.http.server.callbacks

import com.hexagonkt.http.model.NOT_FOUND_404
import java.lang.IllegalArgumentException
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith

internal class CorsCallbackTest {

    @Test fun `Preflight status must be a success one`() {
        val e = assertFailsWith<IllegalArgumentException> {
            CorsCallback(preFlightStatus = NOT_FOUND_404)
        }
        assertEquals("Preflight Status must be a success status: CLIENT_ERROR", e.message)
    }
}
