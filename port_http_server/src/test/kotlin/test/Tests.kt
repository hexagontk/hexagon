package com.hexagonkt.http.server.test

import com.hexagonkt.http.server.Call

// TODO Create TestCall data class to be consistent
fun testCall(
    request: TestRequest = TestRequest(),
    response: TestResponse = TestResponse(),
    session: TestSession = TestSession()) =
        Call(MockRequest(request), MockResponse(response), MockSession(session))
