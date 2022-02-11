package com.hexagonkt.http.server.model

import com.hexagonkt.http.model.HttpCall

data class HttpServerCall(
    override val request: HttpServerRequestPort = HttpServerRequest(),
    override val response: HttpServerResponse = HttpServerResponse(),
) : HttpCall<HttpServerRequestPort, HttpServerResponse>
