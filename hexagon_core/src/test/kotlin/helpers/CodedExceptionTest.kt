package com.hexagonkt.helpers

import org.testng.annotations.Test

@Test class CodedExceptionTest {
    fun `CodedException contains a list of causes`() {
        val causes = (0..9).map { RuntimeException (it.toString()) }
        val exception = CodedException(1, "Coded exception", *causes.toTypedArray())
        assert (exception.causes.size == 10)
        assert (exception.code == 1)
    }
}
