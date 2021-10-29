package com.hexagonkt.http

import java.io.InputStream

data class Part (
    val contentType: String? = null,
    val headers: Map<String, List<String>> = emptyMap(),
    val inputStream: InputStream,
    val name: String,
    val size: Long = -1L,
    val submittedFileName: String? = null
) {
    constructor(name: String, value: String) :
        this(name = name, inputStream = value.byteInputStream(), size = value.length.toLong())

    constructor(name: String, inputStream: InputStream, submittedFileName: String) :
        this(null, name = name, inputStream = inputStream, submittedFileName = submittedFileName)
}
