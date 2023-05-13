package com.hexagonkt.handlers.async

import java.util.concurrent.CompletableFuture
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertIs
import kotlin.test.assertTrue
import kotlin.test.fail

internal class HandlerTest {

    internal companion object {
        fun <T : Any> Handler<T>.process(event: T): CompletableFuture<T> =
            process(EventContext(event, predicate)).thenApply(Context<T>::event)
    }

    @Test fun `Calling next in the last handler returns the last context`() {

        val chain = ChainHandler<String>(
            OnHandler { it.with(event = it.event + "_").done() },
            FilterHandler {
                it.next().thenCompose { c -> c.next() }
            },
        )

        assertEquals("a_", chain.process("a").get())
        assertEquals("b_", chain.process("b").get())
    }

    @Test fun `Error in a filter returns proper exception in context`() {
        val filter = FilterHandler<String> { error("failure") }
        assertEquals("failure", filter.process(EventContext("a", filter.predicate)).get().exception?.message)
    }

    @Test fun `When a callback completes, then the result is returned`() {
        val filter = FilterHandler<String> { it.with(it.event + ":OK").done() }
        assertEquals("Message:OK", filter.process("Message").get())
    }

    @Test fun `When a callback fail, then the context contains the exception`() {
        listOf<Handler<Unit>>(
            FilterHandler { error("Filter Failure") },
            OnHandler { error("Before Failure") },
            AfterHandler { error("After Failure") },
        )
        .forEach {
            val exception = it.process(EventContext(Unit, it.predicate)).get().exception
            val message = exception?.message ?: fail("Exception message missing")
            assertIs<IllegalStateException>(exception)
            assertTrue(message.matches("(Filter|Before|After) Failure".toRegex()))
        }
    }
}
