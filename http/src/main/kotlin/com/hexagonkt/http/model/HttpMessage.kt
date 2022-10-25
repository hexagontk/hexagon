package com.hexagonkt.http.model

interface HttpMessage : HttpBase {
    val cookies: List<Cookie>           // hash of browser cookies

    fun cookiesMap(): Map<String, Cookie> =
        cookies.associateBy { it.name }
}
