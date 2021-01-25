package com.hexagonkt.http.server.test

import com.hexagonkt.http.Cookie
import java.io.ByteArrayOutputStream
import java.io.OutputStream

data class TestResponse(
    var cookies: Map<String, Cookie> = emptyMap(),
    var body: Any = "",
    var contentType: String? = null,
    var status: Int = 200,
    var outputStream: OutputStream = ByteArrayOutputStream()
)
