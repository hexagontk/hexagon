package com.hexagonkt.http.server.handlers

import com.hexagonkt.http.model.HttpMethod.GET
import kotlin.test.Test
import kotlin.test.assertEquals

internal class AfterHandlerTest {

    @Test fun `AfterHandler constructors without path pattern works properly`() {
        val handler1 = AfterHandler(GET) { ok() }
        val handler2 = AfterHandler(setOf(GET)) { ok() }

        assertEquals(handler1.serverPredicate, handler2.serverPredicate)
    }

    @Test fun `AfterHandler constructors with pattern works properly`() {
        val handler1 = AfterHandler(emptySet(), "/a") { ok() }
        val handler2 = AfterHandler("/a") { ok() }

        assertEquals(handler1.serverPredicate, handler2.serverPredicate)
    }
}
