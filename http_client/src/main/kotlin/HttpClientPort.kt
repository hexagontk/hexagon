package com.hexagonkt.http.client

import com.hexagonkt.http.client.model.HttpClientRequest
import com.hexagonkt.http.client.model.HttpClientResponse

interface HttpClientPort {

    fun startUp(client: HttpClient)

    fun shutDown()

    suspend fun send(request: HttpClientRequest): HttpClientResponse
}
