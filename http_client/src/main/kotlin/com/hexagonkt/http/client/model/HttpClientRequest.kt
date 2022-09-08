package com.hexagonkt.http.client.model

import com.hexagonkt.http.*
import com.hexagonkt.http.model.*
import com.hexagonkt.http.model.HttpMethod.GET
import com.hexagonkt.http.model.HttpProtocol.HTTP

data class HttpClientRequest(
    override val method: HttpMethod = GET,
    override val protocol: HttpProtocol = HTTP,
    override val host: String = "localhost",
    override val port: Int = 80,
    override val path: String = "",
    override val queryParameters: HttpFields<QueryParameter> = HttpFields(),
    override val headers: HttpFields<Header> = HttpFields(),
    override val body: Any = "",
    override val parts: List<HttpPart> = emptyList(),
    override val formParameters: HttpFields<FormParameter> = HttpFields(),
    override val cookies: List<HttpCookie> = emptyList(),
    override val contentType: ContentType? = null,
    override val accept: List<ContentType> = emptyList(),
    override val authorization: HttpAuthorization? = null,
) : HttpRequest {

    init {
        checkHeaders(headers)
    }
}
