package com.hexagonkt.helpers

import org.testng.annotations.Test

@Test class MultipleExceptionTest {

    @Test fun `'MultipleException' contains a list of causes`() {
        val causes = (0..9).map { RuntimeException (it.toString()) }
        val exception = MultipleException("Coded exception", *causes.toTypedArray())

        assert (exception.causes.size == 10)
        assert (exception.message == "Coded exception")
        exception.causes.forEachIndexed { ii, e -> assert(e.message == ii.toString()) }

        val exceptionVararg = MultipleException(*causes.toTypedArray())

        assert (exceptionVararg.causes.size == 10)
        exceptionVararg.causes.forEachIndexed { ii, e -> assert(e.message == ii.toString()) }
    }
}
