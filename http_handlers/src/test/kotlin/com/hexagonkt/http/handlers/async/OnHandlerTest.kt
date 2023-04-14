package com.hexagonkt.http.handlers.async

import com.hexagonkt.handlers.async.done
import com.hexagonkt.http.model.HttpMethod.GET
import kotlin.test.Test
import kotlin.test.assertEquals

internal class OnHandlerTest {

    @Test fun `OnHandler constructors works properly`() {
        val handler1 = OnHandler(GET) { ok().done() }
        val handler2 = OnHandler(setOf(GET)) { ok().done() }

        assertEquals(handler1.predicate, handler2.predicate)
    }
}
