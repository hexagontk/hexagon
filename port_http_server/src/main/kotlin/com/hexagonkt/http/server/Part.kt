package com.hexagonkt.http.server

import java.io.InputStream

data class Part (
    val contentType: String,
    val headers: Map<String, List<String>>,
    val inputStream: InputStream,
    val name: String,
    val size: Long,
    val submittedFileName: String
)
