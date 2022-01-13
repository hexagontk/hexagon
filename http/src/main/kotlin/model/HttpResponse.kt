package com.hexagonkt.http.model

interface HttpResponse : HttpMessage {
    val status: HttpStatus
}
