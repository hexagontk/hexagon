package com.hexagonkt.http.model

import com.hexagonkt.http.formatQueryString
import java.net.URL

// TODO 'formParameters' are a kind of 'part' and both are handled as part of the 'body'
//  they could be handled as a special kind of type in body processing (List<HttpPartPort>)
interface HttpRequest : HttpMessage {
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

    fun partsMap(): Map<String, HttpPart> =
        parts.associateBy { it.name }

    fun url(): URL =
        if (queryParameters.isEmpty())
            URL("${protocol.schema}://$host:$port/$path")
        else
            URL("${protocol.schema}://$host:$port/$path?${formatQueryString(queryParameters)}")

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
