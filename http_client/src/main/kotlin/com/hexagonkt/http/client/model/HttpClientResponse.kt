package com.hexagonkt.http.client.model

import com.hexagonkt.http.model.NOT_FOUND_404
import com.hexagonkt.http.checkHeaders
import com.hexagonkt.http.model.*

data class HttpClientResponse(
    override val body: Any = "",
    override val headers: Headers = Headers(),
    override val contentType: ContentType? = null,
    override val cookies: List<Cookie> = emptyList(),
    override val status: HttpStatus = NOT_FOUND_404,
    override val contentLength: Long = -1L
) : HttpClientResponsePort {

    init {
        checkHeaders(headers)
    }
}
