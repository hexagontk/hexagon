package com.hexagonkt.handlers

import org.junit.jupiter.api.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull

internal class ExceptionHandlerTest {

    @Test fun `Exceptions are cleared properly`() {
        ChainHandler(
            ExceptionHandler<String, Exception>(Exception::class) { c, _ -> c.with("ok") },
            OnHandler { error("Error") }
        )
        .process(EventContext("test", { true }))
        .let {
            assertEquals("ok", it.event)
            assertNull(it.exception)
        }

        ChainHandler(
            ExceptionHandler<String, Exception>(Exception::class) { c, _ -> c },
            OnHandler { error("Error") }
        )
        .process(EventContext("test", { true }))
        .let {
            assertEquals("test", it.event)
            assertNull(it.exception)
        }

        ChainHandler(
            ExceptionHandler<String, Exception>(Exception::class, false) { c, _ -> c.with("ok") },
            OnHandler { error("Error") }
        )
        .process(EventContext("test", { true }))
        .let {
            assertEquals("Error", it.exception?.message)
            assert(it.exception is IllegalStateException)
        }

        ChainHandler(
            ExceptionHandler<String, Exception>(Exception::class, false) { c, _ -> c.with("ok") },
            OnHandler { it.with("no problem") }
        )
        .process(EventContext("test", { true }))
        .let {
            assertNull(it.exception)
            assertEquals("no problem", it.event)
        }

        ChainHandler(
            ExceptionHandler<String, Exception>(Exception::class, false) { c, _ -> c.with("ok") },
            ExceptionHandler(Exception::class, false) { _, _ -> error("Fail") },
            OnHandler { error("Error") }
        )
        .process(EventContext("test", { true }))
        .let {
            assertEquals("ok", it.event)
            assertEquals("Fail", it.exception?.message)
            assert(it.exception is IllegalStateException)
        }
    }
}
