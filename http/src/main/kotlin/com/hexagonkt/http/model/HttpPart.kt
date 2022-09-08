package com.hexagonkt.http.model

data class HttpPart (
    val name: String,
    override val body: Any,
    override val headers: HttpFields<Header> = HttpFields(),
    override val contentType: ContentType? = null,
    val size: Long = -1L,
    val submittedFileName: String? = null
) : HttpBase {

    constructor(name: String, value: String) :
        this(name, value, size = value.toByteArray().size.toLong())

    constructor(name: String, body: ByteArray, submittedFileName: String) :
        this(name, body, size = body.size.toLong(), submittedFileName = submittedFileName)
}
