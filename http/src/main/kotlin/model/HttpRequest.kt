package com.hexagonkt.http.model

import com.hexagonkt.core.helpers.MultiMap
import java.net.URL

// TODO 'formParameters' are a kind of 'part' and both are handled as part of the 'body'
//  they could be handled as a special kind of type in body processing (List<HttpPartPort>)
interface HttpRequest : HttpMessage {
    val method: HttpMethod                        // "GET"
    val protocol: HttpProtocol                    // "http"
    val host: String                              // "example.com"
    val port: Int                                 // 80
    val path: String                              // "/foo" servlet path + path info
    val queryString: String                       // ""
    val parts: List<HttpPartPort>                 // hash of multipart parts
    val formParameters: MultiMap<String, String>
    val accept: List<ContentType>

    fun partsMap(): Map<String, HttpPartPort> =
        parts.associateBy { it.name }

    fun url(): URL =
        if (queryString.isBlank())
            URL("${protocol.schema}://$host:$port/$path")
        else
            URL("${protocol.schema}://$host:$port/$path?$queryString")
}
