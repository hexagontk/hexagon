package com.hexagonkt.helpers

import org.testng.annotations.Test
import java.lang.RuntimeException

@Test class CodedExceptionTest {

    @Test fun `Default 'CodedException' has empty message and 'null' cause`() {
        val exception = CodedException(10)

        assert(exception.code == 10)
        assert(exception.message == "")
        assert(exception.cause == null)
    }

    @Test fun `'CodedException' constructors initialize instances properly`() {
        val exception = CodedException(10, "message")

        assert(exception.code == 10)
        assert(exception.message == "message")
        assert(exception.cause == null)

        val cause = RuntimeException()
        val exceptionWithCause = CodedException(10, "message", cause)

        assert(exceptionWithCause.code == 10)
        assert(exceptionWithCause.message == "message")
        assert(exceptionWithCause.cause == cause)

        val exceptionWithoutMessage = CodedException(10, cause = cause)

        assert(exceptionWithoutMessage.code == 10)
        assert(exceptionWithoutMessage.message == "")
        assert(exceptionWithoutMessage.cause == cause)
    }
}
