package com.hexagontk.http.handlers

import com.hexagontk.http.model.HttpMethod.GET
import com.hexagontk.http.model.HttpRequest
import org.junit.jupiter.api.Test

internal class BeforeHandlerTest {

    @Test fun `OnHandler constructors works properly`() {
        val handler1 = BeforeHandler(GET) { ok() }
        val handler2 = BeforeHandler(setOf(GET)) { ok() }

        assertEqualHttpPredicatesFn(handler1.predicate, handler2.predicate)
        assert(!handler1.process(HttpRequest()).handled)
    }
}
