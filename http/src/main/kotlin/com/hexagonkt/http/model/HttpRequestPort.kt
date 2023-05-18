package com.hexagonkt.http.model

import com.hexagonkt.http.formatQueryString
import java.net.URL
import java.security.cert.X509Certificate

// TODO 'formParameters' are a kind of 'part' and both are handled as part of the 'body'
//  they could be handled as a special kind of type in body processing (List<HttpPartPort>)
interface HttpRequestPort : HttpMessage {
    val method: HttpMethod                        // "GET"
    val protocol: HttpProtocol                    // "http"
    val host: String                              // "example.com"
    val port: Int                                 // 80
    val path: String                              // "/foo" servlet path + path info
    val queryParameters: QueryParameters
    val parts: List<HttpPart>                     // hash of multipart parts
    val formParameters: FormParameters
    val accept: List<ContentType>
    val authorization: Authorization?

    val certificateChain: List<X509Certificate>
    val contentLength: Long                        // length of request.body (or 0)

    fun with(
        body: Any = this.body,
        headers: Headers = this.headers,
        contentType: ContentType? = this.contentType,
        method: HttpMethod = this.method,
        protocol: HttpProtocol = this.protocol,
        host: String = this.host,
        port: Int = this.port,
        path: String = this.path,
        queryParameters: QueryParameters = this.queryParameters,
        parts: List<HttpPart> = this.parts,
        formParameters: FormParameters = this.formParameters,
        cookies: List<Cookie> = this.cookies,
        accept: List<ContentType> = this.accept,
        authorization: Authorization? = this.authorization,
        certificateChain: List<X509Certificate> = this.certificateChain,
    ): HttpRequestPort

    operator fun plus(header: Header): HttpRequestPort =
        with(headers = headers + header)

    operator fun plus(queryParameter: QueryParameter): HttpRequestPort =
        with(queryParameters = queryParameters + queryParameter)

    operator fun plus(part: HttpPart): HttpRequestPort =
        with(parts = parts + part)

    operator fun plus(formParameter: FormParameter): HttpRequestPort =
        with(formParameters = formParameters + formParameter)

    operator fun plus(cookie: Cookie): HttpRequestPort =
        with(cookies = cookies + cookie)

    fun certificate(): X509Certificate? =
        certificateChain.firstOrNull()

    fun partsMap(): Map<String, HttpPart> =
        parts.associateBy { it.name }

    fun url(): URL =
        when {
            queryParameters.isEmpty() && port == 80 -> "${protocol.schema}://$host/$path"
            queryParameters.isEmpty() -> "${protocol.schema}://$host:$port/$path"
            else -> "${protocol.schema}://$host:$port/$path?${formatQueryString(queryParameters)}"
        }
        .let(::URL)

    fun userAgent(): String? =
        headers["user-agent"]?.value

    fun referer(): String? =
        headers["referer"]?.value

    fun origin(): String? =
        headers["origin"]?.value

    fun authorization(): Authorization? =
        headers["authorization"]
            ?.value
            ?.split(" ", limit = 2)
            ?.let { Authorization(it.first(), it.last()) }
}
