package com.hexagontk.http.handlers

import com.hexagontk.http.model.HttpMethod.GET
import com.hexagontk.http.model.HttpRequest
import org.junit.jupiter.api.Test
import kotlin.test.assertEquals

internal class OnHandlerTest {

    @Test fun `OnHandler constructors works properly`() {
        val handler1 = OnHandler(GET) { ok() }
        val handler2 = OnHandler(setOf(GET)) { ok() }

        assertEquals(handler1.predicate, handler2.predicate)
        assert(handler1.process(HttpRequest()).handled)
    }
}
