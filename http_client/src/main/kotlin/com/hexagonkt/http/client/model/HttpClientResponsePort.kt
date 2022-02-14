package com.hexagonkt.http.client.model

import com.hexagonkt.http.model.HttpResponse

interface HttpClientResponsePort : HttpResponse {
    val contentLength: Long                        // length of response.body (or 0)
}
