package com.hexagontk.http.model

class HttpCall(
    var request: HttpRequestPort = HttpRequest(),
    var response: HttpResponsePort = HttpResponse(),
)
