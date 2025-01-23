package com.hexagontk.http.handlers

import com.hexagontk.http.model.HttpMethod.GET
import org.junit.jupiter.api.Test

internal class FilterHandlerTest {

    @Test fun `FilterHandler constructors without path pattern works properly`() {
        val handler1 = FilterHandler(GET) { ok() }
        val handler2 = FilterHandler(setOf(GET)) { ok() }

        assertEqualHttpPredicatesFn(handler1.predicate, handler2.predicate)
    }

    @Test fun `FilterHandler constructors with pattern works properly`() {
        val handler1 = FilterHandler(emptySet(), "/a") { ok() }
        val handler2 = FilterHandler("/a") { ok() }

        assertEqualHttpPredicatesFn(handler1.predicate, handler2.predicate)
    }
}
