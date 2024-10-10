package com.hexagontk.handlers

import org.junit.jupiter.api.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith

internal class HandlersTest {

    @Test fun `Exceptions are casted properly`() {
        assertFailsWith<IllegalStateException> {
            ExceptionHandler.castException(null, Exception::class)
        }
        assertFailsWith<ClassCastException> {
            ExceptionHandler.castException(IllegalStateException(), IllegalArgumentException::class)
        }

        val ise = IllegalStateException()
        assertEquals(ise, ExceptionHandler.castException(ise, RuntimeException::class))
    }
}
