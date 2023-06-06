package com.hexagonkt.http.handlers.async

import com.hexagonkt.handlers.async.done
import com.hexagonkt.http.model.HttpMethod.GET
import kotlin.test.Test
import kotlin.test.assertEquals

internal class AfterHandlerTest {

    @Test fun `AfterHandler constructors without path pattern works properly`() {
        val handler1 = AfterHandler(GET) { ok().done() }
        val handler2 = AfterHandler(setOf(GET)) { ok().done() }

        assertEquals(handler1.predicate, handler2.predicate)
    }

    @Test fun `AfterHandler constructors with pattern works properly`() {
        val handler1 = AfterHandler(emptySet(), "/a") { ok().done() }
        val handler2 = AfterHandler("/a") { ok().done() }

        assertEquals(handler1.predicate, handler2.predicate)
    }
}
