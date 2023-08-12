package com.hexagonkt.http.handlers

import com.hexagonkt.http.model.HttpMethod.GET
import com.hexagonkt.http.model.HttpRequest
import kotlin.test.Test
import kotlin.test.assertEquals

internal class BeforeHandlerTest {

    @Test fun `OnHandler constructors works properly`() {
        val handler1 = BeforeHandler(GET) { ok() }
        val handler2 = BeforeHandler(setOf(GET)) { ok() }

        assertEquals(handler1.predicate, handler2.predicate)
        assert(!handler1.process(HttpRequest()).handled)
    }
}
