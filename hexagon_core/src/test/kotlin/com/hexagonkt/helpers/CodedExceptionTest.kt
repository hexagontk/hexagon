package com.hexagonkt.helpers

import org.testng.annotations.Test

@Test class CodedExceptionTest {

    @Test fun `Default 'CodedException' has empty message and 'null' cause`() {
        val exception = CodedException(10)

        assert(exception.code == 10)
        assert(exception.message == "")
        assert(exception.cause == null)
    }
}
