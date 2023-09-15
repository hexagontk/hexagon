package com.hexagonkt.core

import kotlin.test.Test
import kotlin.test.assertContentEquals
import kotlin.test.assertEquals

internal class MultipleExceptionTest {

    @Test fun `'MultipleException' contains a list of causes`() {
        val causes = (0..9).map { RuntimeException (it.toString()) }
        val exception = MultipleException("Coded exception", *causes.toTypedArray())
        val exception2 = MultipleException(causes, "Coded exception")

        assertEquals(exception.message, exception2.message)
        assertContentEquals(exception.causes, exception2.causes)
        assertEquals(10, exception.causes.size)
        assertEquals("Coded exception", exception.message)
        exception.causes.forEachIndexed { ii, e -> assertEquals(e.message, ii.toString()) }

        val exceptionVararg = MultipleException(*causes.toTypedArray())

        assertEquals(10, exceptionVararg.causes.size)
        exceptionVararg.causes.forEachIndexed { ii, e -> assertEquals(ii.toString(), e.message) }
    }
}
