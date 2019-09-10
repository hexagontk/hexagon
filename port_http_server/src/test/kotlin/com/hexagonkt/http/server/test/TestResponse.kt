package com.hexagonkt.http.server.test

import java.io.ByteArrayOutputStream
import java.io.OutputStream
import java.net.HttpCookie

data class TestResponse(
    var cookies: Map<String, HttpCookie> = emptyMap(),
    var body: Any = "",
    var contentType: String? = null,
    var status: Int = 200,
    var outputStream: OutputStream = ByteArrayOutputStream()
)
