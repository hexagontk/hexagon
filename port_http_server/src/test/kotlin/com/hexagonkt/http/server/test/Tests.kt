package com.hexagonkt.http.server.test

import com.hexagonkt.http.server.Call

fun testCall(
    request: TestRequest = TestRequest(),
    response: TestResponse = TestResponse(),
    session: TestSession = TestSession()) =
        Call(MockRequest(request), MockResponse(response), MockSession(session))
