package com.hexagonkt.http.server.model

import com.hexagonkt.http.*
import com.hexagonkt.http.model.*
import com.hexagonkt.http.model.ClientErrorStatus.NOT_FOUND

data class HttpServerResponse(
    override val body: Any = "",
    override val headers: HttpFields<Header> = HttpFields(),
    override val contentType: ContentType? = null,
    override val cookies: List<HttpCookie> = emptyList(),
    override val status: HttpStatus = NOT_FOUND,
) : HttpResponse {

    init {
        checkHeaders(headers)
    }
}
