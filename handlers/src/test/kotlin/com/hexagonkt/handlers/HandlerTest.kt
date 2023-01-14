package com.hexagonkt.handlers

import com.hexagonkt.core.logging.LoggingLevel.OFF
import com.hexagonkt.core.logging.LoggingLevel.TRACE
import com.hexagonkt.core.logging.LoggingManager
import org.junit.jupiter.api.AfterAll
import org.junit.jupiter.api.BeforeAll
import kotlin.test.Test
import org.junit.jupiter.api.TestInstance
import org.junit.jupiter.api.TestInstance.Lifecycle.PER_CLASS
import kotlin.test.assertEquals
import kotlin.test.assertIs
import kotlin.test.assertTrue
import kotlin.test.fail

@TestInstance(PER_CLASS)
internal class HandlerTest {

    @BeforeAll fun enableLogging() {
        LoggingManager.setLoggerLevel(OFF)
        LoggingManager.setLoggerLevel("com.hexagonkt.core", TRACE)
    }

    @AfterAll fun disableLogging() {
        LoggingManager.setLoggerLevel(OFF)
    }

    @Test fun `Calling next in the last handler returns the last context`() {

        val chain = ChainHandler<String>(
            OnHandler { it.copy(event = it.event + "_") },
            FilterHandler {
                it.next().next()
            },
        )

        assertEquals("a_", chain.process("a"))
        assertEquals("b_", chain.process("b"))
    }

    @Test fun `Error in a filter returns proper exception in context`() {
        val filter = FilterHandler<String> { error("failure") }
        assertEquals("failure", filter.process(Context("a", filter.predicate)).exception?.message)
    }

    @Test fun `When a callback completes, then the result is returned`() {
        val filter = FilterHandler<String> { it.copy(it.event + ":OK") }
        assertEquals("Message:OK", filter.process("Message"))
    }

    @Test fun `When a callback fail, then the context contains the exception`() {
        listOf<Handler<Unit>>(
            FilterHandler { error("Filter Failure") },
            OnHandler { error("Before Failure") },
            AfterHandler { error("After Failure") },
        )
        .forEach {
            val exception = it.process(Context(Unit, it.predicate)).exception
            val message = exception?.message ?: fail("Exception message missing")
            assertIs<IllegalStateException>(exception)
            assertTrue(message.matches("(Filter|Before|After) Failure".toRegex()))
        }
    }
}
