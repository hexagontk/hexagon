package com.hexagonkt.handlers

import org.junit.jupiter.api.Test
import kotlin.test.*

internal class HandlerTest {

    internal companion object {
        fun <T : Any> Handler<T>.process(event: T): T =
            process(EventContext(event, predicate)).event
    }

    @Test fun `Calling next in the last handler returns the last context`() {

        val chain = ChainHandler<String>(
            OnHandler { it.with(event = it.event + "_") },
            FilterHandler {
                it.next().next()
            },
        )

        assertEquals("a_", chain.process("a"))
        assertEquals("b_", chain.process("b"))
    }

    @Test fun `Error in a filter returns proper exception in context`() {
        val filter = FilterHandler<String> { error("failure") }
        assertEquals("failure", filter.process(EventContext("a", filter.predicate)).exception?.message)
    }

    @Test fun `When a callback completes, then the result is returned`() {
        val filter = FilterHandler<String> { it.with(it.event + ":OK") }
        assertEquals("Message:OK", filter.process("Message"))
    }

    @Test fun `When a callback fail, then the context contains the exception`() {
        listOf<Handler<Unit>>(
            FilterHandler { error("Filter Failure") },
            OnHandler { error("Before Failure") },
            AfterHandler { error("After Failure") },
        )
        .forEach {
            val exception = it.process(EventContext(Unit, it.predicate)).exception
            val message = exception?.message ?: fail("Exception message missing")
            assertIs<IllegalStateException>(exception)
            assertTrue(message.matches("(Filter|Before|After) Failure".toRegex()))
        }
    }
}
