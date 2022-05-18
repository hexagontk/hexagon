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
    val queryParameters: HttpFields<QueryParameter>
    val parts: List<HttpPartPort>                 // hash of multipart parts
    val formParameters: HttpFields<FormParameter>
    val accept: List<ContentType>

    fun partsMap(): Map<String, HttpPartPort> =
        parts.associateBy { it.name }

    fun url(): URL =
        if (queryParameters.isEmpty())
            URL("${protocol.schema}://$host:$port/$path")
        else
            URL("${protocol.schema}://$host:$port/$path?${formatQueryString(queryParameters)}")

    fun userAgent(): String? =
        headers["user-agent"]

    fun referer(): String? =
        headers["referer"]

    fun origin(): String? =
        headers["origin"]
}
