package com.hexagontk.http.model

data class HttpCall(
    val request: HttpRequestPort = HttpRequest(),
    val response: HttpResponsePort = HttpResponse(),
)
