package com.hexagonkt.helpers

import org.testng.annotations.Test

@Test class CodedExceptionTest {

    @Test fun `'CodedException' contains a list of causes`() {
        val causes = (0..9).map { RuntimeException (it.toString()) }
        val exception = CodedException(1, "Coded exception", *causes.toTypedArray())

        assert (exception.causes.size == 10)
        assert (exception.message == "Coded exception")
        assert (exception.code == 1)

        exception.causes.forEachIndexed { ii, e ->
            assert(e.message == ii.toString())
        }
    }

    @Test fun `Default 'CodedException' has 0 code and 'null' cause`() {
        val exception = CodedException("message")

        assert(exception.code == 0)
        assert(exception.message == "message")
        assert(exception.cause == null)
        assert(exception.causes.isEmpty())
    }
}
