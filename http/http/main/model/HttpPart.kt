package com.hexagontk.http.model

class HttpPart (
    val name: String,
    override val body: Any,
    override val headers: Headers = Headers(),
    override val contentType: ContentType? = null,
    val size: Long = -1L,
    val submittedFileName: String? = null
) : HttpBase {

    constructor(name: String, value: String) :
        this(name, value, size = value.toByteArray().size.toLong())

    constructor(name: String, body: ByteArray, submittedFileName: String) :
        this(name, body, size = body.size.toLong(), submittedFileName = submittedFileName)

    fun with(
        name: String = this.name,
        body: Any  = this.body,
        headers: Headers = this.headers,
        contentType: ContentType? = this.contentType,
        size: Long = this.size,
        submittedFileName: String? = this.submittedFileName
    ): HttpPart =
        HttpPart(name, body, headers, contentType, size, submittedFileName)
}
