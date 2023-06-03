package com.hexagonkt.http.handlers.async

import com.hexagonkt.handlers.async.done
import com.hexagonkt.http.model.HttpMethod.GET
import kotlin.test.Test
import kotlin.test.assertEquals

internal class FilterHandlerTest {

    @Test fun `FilterHandler constructors without path pattern works properly`() {
        val handler1 = FilterHandler(GET) { ok().done() }
        val handler2 = FilterHandler(setOf(GET)) { ok().done() }

        assertEquals(handler1.predicate, handler2.predicate)
    }

    @Test fun `FilterHandler constructors with pattern works properly`() {
        val handler1 = FilterHandler(emptySet(), "/a") { ok().done() }
        val handler2 = FilterHandler("/a") { ok().done() }

        assertEquals(handler1.predicate, handler2.predicate)
    }
}
