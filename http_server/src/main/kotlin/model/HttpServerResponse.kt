package com.hexagonkt.http.server.model

import com.hexagonkt.core.helpers.MultiMap
import com.hexagonkt.core.helpers.multiMapOf
import com.hexagonkt.http.*
import com.hexagonkt.http.model.ClientErrorStatus.NOT_FOUND
import com.hexagonkt.http.model.ContentType
import com.hexagonkt.http.model.HttpCookie
import com.hexagonkt.http.model.HttpResponse
import com.hexagonkt.http.model.HttpStatus

data class HttpServerResponse(
    override val body: Any = "",
    override val headers: MultiMap<String, String> = multiMapOf(),
    override val contentType: ContentType? = null,
    override val cookies: List<HttpCookie> = emptyList(),
    override val status: HttpStatus = NOT_FOUND,
) : HttpResponse {

    init {
        checkHeaders(headers)
    }
}
