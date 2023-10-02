package com.hexagonkt.handlers

import org.junit.jupiter.api.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith

internal class HandlersTest {

    @Test fun `Exceptions are casted properly`() {
        assertFailsWith<IllegalStateException> { castException(null, Exception::class) }
        assertFailsWith<ClassCastException> {
            castException(IllegalStateException(), IllegalArgumentException::class)
        }

        val ise = IllegalStateException()
        assertEquals(ise, castException(ise, RuntimeException::class))
    }
}
