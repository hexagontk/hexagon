package com.hexagonkt.http.model

interface HttpCall<I : HttpRequest, O : HttpResponse> {
    val request: I
    val response: O
}
