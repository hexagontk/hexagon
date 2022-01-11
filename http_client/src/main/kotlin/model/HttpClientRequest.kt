package com.hexagonkt.http.client.model

import com.hexagonkt.core.MultiMap
import com.hexagonkt.core.multiMapOf
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
    override val queryString: String = "",
    override val headers: MultiMap<String, String> = multiMapOf(),
    override val body: Any = "",
    override val parts: List<HttpPart> = emptyList(),
    override val formParameters: MultiMap<String, String> = multiMapOf(),
    override val cookies: List<HttpCookie> = emptyList(),
    override val contentType: ContentType? = null,
    override val accept: List<ContentType> = emptyList(),
) : HttpRequest {

    init {
        checkHeaders(headers)
    }
}
