package com.hexagontk.http.model

class HttpCall(
    val request: HttpRequestPort = HttpRequest(),
    val response: HttpResponsePort = HttpResponse(),
)
