package com.hexagonkt.http.handlers.coroutines

import com.hexagonkt.http.model.HttpMethod.GET
import com.hexagonkt.http.model.HttpRequest
import kotlinx.coroutines.runBlocking
import kotlin.test.Test
import kotlin.test.assertEquals

internal class OnHandlerTest {

    @Test fun `OnHandler constructors works properly`() = runBlocking {
        val handler1 = OnHandler(GET) { ok() }
        val handler2 = OnHandler(setOf(GET)) { ok() }

        assertEquals(handler1.predicate, handler2.predicate)
        assert(handler1.process(HttpRequest()).handled)
    }
}
