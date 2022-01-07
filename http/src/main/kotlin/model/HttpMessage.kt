package com.hexagonkt.http.model

interface HttpMessage : HttpBase {
    val cookies: List<HttpCookie>           // hash of browser cookies

    fun cookiesMap(): Map<String, HttpCookie> =
        cookies.associateBy { it.name }
}
