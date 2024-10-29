package com.hexagontk.http.server.callbacks

import com.hexagontk.http.model.NOT_FOUND_404
import java.lang.IllegalArgumentException
import org.junit.jupiter.api.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith

internal class CorsCallbackTest {

    @Test fun `Preflight status must be a success one`() {
        val e = assertFailsWith<IllegalArgumentException> {
            CorsCallback(preFlightStatus = NOT_FOUND_404)
        }
        assertEquals("Preflight Status must be a success status: 404", e.message)
    }
}
