package com.hexagonkt.serverless.http

import com.hexagonkt.http.handlers.HttpHandler
import com.hexagonkt.http.model.HttpRequestPort
import com.hexagonkt.http.model.HttpResponsePort

interface ServerlessHttp {
    val handler: HttpHandler
    fun request(): HttpRequestPort
    fun response(): HttpResponsePort
}
