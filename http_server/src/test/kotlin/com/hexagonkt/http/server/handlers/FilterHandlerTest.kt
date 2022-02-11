package com.hexagonkt.http.server.handlers

import com.hexagonkt.http.model.HttpMethod.GET
import org.junit.jupiter.api.Test
import kotlin.test.assertEquals

internal class FilterHandlerTest {

    @Test fun `FilterHandler constructors without path pattern works properly`() {
        val handler1 = FilterHandler(GET) { ok() }
        val handler2 = FilterHandler(setOf(GET)) { ok() }

        assertEquals(handler1.serverPredicate, handler2.serverPredicate)
    }

    @Test fun `FilterHandler constructors with pattern works properly`() {
        val handler1 = FilterHandler(emptySet(), "/a") { ok() }
        val handler2 = FilterHandler("/a") { ok() }

        assertEquals(handler1.serverPredicate, handler2.serverPredicate)
    }
}
