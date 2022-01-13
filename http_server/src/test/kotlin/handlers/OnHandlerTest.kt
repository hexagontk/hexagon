package com.hexagonkt.http.server.handlers

import com.hexagonkt.http.model.HttpMethod.GET
import org.junit.jupiter.api.Test
import kotlin.test.assertEquals

internal class OnHandlerTest {

    @Test fun `OnHandler constructors works properly`() {
        val handler1 = OnHandler(GET) { ok() }
        val handler2 = OnHandler(setOf(GET)) { ok() }

        assertEquals(handler1.serverPredicate, handler2.serverPredicate)
    }
}
