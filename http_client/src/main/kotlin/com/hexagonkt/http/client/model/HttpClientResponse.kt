package com.hexagonkt.http.client.model

import com.hexagonkt.core.MultiMap
import com.hexagonkt.http.model.ContentType
import com.hexagonkt.http.model.HttpCookie
import com.hexagonkt.http.model.HttpStatus
import com.hexagonkt.http.model.ClientErrorStatus.NOT_FOUND
import com.hexagonkt.http.checkHeaders

data class HttpClientResponse(
    override val body: Any = "",
    override val headers: MultiMap<String, String> = MultiMap(emptyMap()),
    override val contentType: ContentType? = null,
    override val cookies: List<HttpCookie> = emptyList(),
    override val status: HttpStatus = NOT_FOUND,
    override val contentLength: Long = -1L
) : HttpClientResponsePort {

    init {
        checkHeaders(headers)
    }
}
