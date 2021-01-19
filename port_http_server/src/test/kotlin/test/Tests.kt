package com.hexagonkt.http.server.test

import com.hexagonkt.http.server.Call
import com.hexagonkt.http.server.Request
import com.hexagonkt.http.server.Response
import com.hexagonkt.http.server.Session

// TODO Create TestCall data class to be consistent
fun testCall(
    request: TestRequest = TestRequest(),
    response: TestResponse = TestResponse(),
    session: TestSession = TestSession()) =
        Call(
            Request(MockRequest(request)),
            Response(MockResponse(response)),
            Session(MockSession(session))
        )
