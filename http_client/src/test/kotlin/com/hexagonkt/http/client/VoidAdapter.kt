package com.hexagonkt.http.client

import com.hexagonkt.http.client.model.HttpClientRequest
import com.hexagonkt.http.client.model.HttpClientResponse
import com.hexagonkt.http.model.Header

object VoidAdapter : HttpClientPort {
    var started: Boolean = false

    override fun startUp(client: HttpClient) {
        started = true
    }

    override fun shutDown() {
        started = false
    }

    override fun send(request: HttpClientRequest): HttpClientResponse =
        HttpClientResponse(
            headers = request.headers + Header("-path-", request.path),
            body = request.body,
            contentType = request.contentType,
        )
}
