package com.hexagontk.http.model

import com.hexagontk.core.urlOf
import com.hexagontk.http.formatQueryString
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
    val queryParameters: Parameters
    val parts: List<HttpPart>                     // hash of multipart parts
    val formParameters: Parameters
    val accept: List<ContentType>
    val authorization: Authorization?

    val certificateChain: List<X509Certificate>
    val contentLength: Long                       // length of request.body (or 0)
    // TODO
//    val pathPattern: PathPattern?
//    val pathParameters: Map<String, String>

    fun with(
        body: Any = this.body,
        headers: Headers = this.headers,
        contentType: ContentType? = this.contentType,
        method: HttpMethod = this.method,
        protocol: HttpProtocol = this.protocol,
        host: String = this.host,
        port: Int = this.port,
        path: String = this.path,
        queryParameters: Parameters = this.queryParameters,
        parts: List<HttpPart> = this.parts,
        formParameters: Parameters = this.formParameters,
        cookies: List<Cookie> = this.cookies,
        accept: List<ContentType> = this.accept,
        authorization: Authorization? = this.authorization,
        certificateChain: List<X509Certificate> = this.certificateChain,
    ): HttpRequestPort

    operator fun plus(header: Field): HttpRequestPort =
        with(headers = headers + header)

    operator fun plus(part: HttpPart): HttpRequestPort =
        with(parts = parts + part)

    operator fun plus(cookie: Cookie): HttpRequestPort =
        with(cookies = cookies + cookie)

    operator fun plus(headers: Headers): HttpRequestPort =
        with(headers = this.headers + headers)

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
        .let(::urlOf)

    fun userAgent(): String? =
        headers["user-agent"]?.text

    fun referer(): String? =
        headers["referer"]?.text

    fun origin(): String? =
        headers["origin"]?.text

    fun authorization(): Authorization? =
        headers["authorization"]?.text
            ?.split(" ", limit = 2)
            ?.let { Authorization(it.first(), it.last()) }
}
