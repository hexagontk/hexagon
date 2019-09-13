package com.hexagonkt.http.server

import com.hexagonkt.http.server.test.testCall
import com.hexagonkt.http.server.test.TestRequest
import org.testng.annotations.Test

internal class TestsTest {

    private fun Call.weird() {
        if (request.body == "weird")
            send(999, "Weird error")
    }

    @Test fun `Test call behaves as expected`() {
        val testCall = testCall(TestRequest(body = "weird"))
        testCall.weird()
        assert(testCall.response.status == 999)
        assert(testCall.response.body == "Weird error")
    }
}
