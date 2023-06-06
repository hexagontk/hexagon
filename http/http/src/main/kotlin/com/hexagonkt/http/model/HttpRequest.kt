package com.hexagonkt.http.model

import com.hexagonkt.http.*
import com.hexagonkt.http.model.HttpMethod.GET
import com.hexagonkt.http.model.HttpProtocol.HTTP
import java.security.cert.X509Certificate

data class HttpRequest(
    override val method: HttpMethod = GET,
    override val protocol: HttpProtocol = HTTP,
    override val host: String = "localhost",
    override val port: Int = 80,
    override val path: String = "",
    override val queryParameters: QueryParameters = QueryParameters(),
    override val headers: Headers = Headers(),
    override val body: Any = "",
    override val parts: List<HttpPart> = emptyList(),
    override val formParameters: FormParameters = FormParameters(),
    override val cookies: List<Cookie> = emptyList(),
    override val contentType: ContentType? = null,
    override val certificateChain: List<X509Certificate> = emptyList(),
    override val accept: List<ContentType> = emptyList(),
    override val contentLength: Long = -1L,
    override val authorization: Authorization? = null,
) : HttpRequestPort {

    init {
        checkHeaders(headers)
    }

    override fun with(
        body: Any,
        headers: Headers,
        contentType: ContentType?,
        method: HttpMethod,
        protocol: HttpProtocol,
        host: String,
        port: Int,
        path: String,
        queryParameters: QueryParameters,
        parts: List<HttpPart>,
        formParameters: FormParameters,
        cookies: List<Cookie>,
        accept: List<ContentType>,
        authorization: Authorization?,
        certificateChain: List<X509Certificate>,
    ): HttpRequestPort =
        copy(
            body = body,
            headers = headers,
            contentType = contentType,
            method = method,
            protocol = protocol,
            host = host,
            port = port,
            path = path,
            queryParameters = queryParameters,
            parts = parts,
            formParameters = formParameters,
            cookies = cookies,
            accept = accept,
            authorization = authorization,
            certificateChain = certificateChain,
        )
}
