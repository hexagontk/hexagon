package com.hexagonkt.http.client.model

import com.hexagonkt.http.model.ClientErrorStatus.NOT_FOUND
import com.hexagonkt.http.checkHeaders
import com.hexagonkt.http.model.*

data class HttpClientResponse(
    override val body: Any = "",
    override val headers: Headers = Headers(),
    override val contentType: ContentType? = null,
    override val cookies: List<HttpCookie> = emptyList(),
    override val status: HttpStatus = NOT_FOUND,
    override val contentLength: Long = -1L
) : HttpClientResponsePort {

    init {
        checkHeaders(headers)
    }
}
