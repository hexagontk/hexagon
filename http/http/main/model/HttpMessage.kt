package com.hexagontk.http.model

interface HttpMessage : HttpBase {
    // TODO Use cookies from headers
    val cookies: List<Cookie>           // hash of browser cookies

    fun cookiesMap(): Map<String, Cookie> =
        cookies.associateBy { it.name }
}
