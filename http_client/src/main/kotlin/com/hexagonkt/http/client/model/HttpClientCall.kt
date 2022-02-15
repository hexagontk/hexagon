package com.hexagonkt.http.client.model

import com.hexagonkt.http.model.HttpCall

data class HttpClientCall(
    override val request: HttpClientRequest = HttpClientRequest(),
    override val response: HttpClientResponsePort = HttpClientResponse(),
) : HttpCall<HttpClientRequest, HttpClientResponsePort>
