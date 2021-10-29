package com.hexagonkt.http.server

import com.hexagonkt.http.Cookie
import com.hexagonkt.http.Method
import com.hexagonkt.http.Part
import java.security.cert.X509Certificate

interface RequestPort {

    fun method(): Method                             // "GET"
    fun scheme(): String                             // "http"
    fun host(): String                               // "example.com"
    fun ip(): String                                 // client IP address
    fun port(): Int                                  // 80
    fun path(): String                               // "/foo" servlet path + path info
    fun pathParameters(): Map<String, String>        // ["p"] "p" path parameter
    fun queryString(): String                        // ""
    fun url(): String                                // "http://example.com/example/foo"
    fun parts(): Map<String, Part>                   // hash of multipart parts
    fun queryParameters(): Map<String, List<String>>
    fun formParameters(): Map<String, List<String>>
    fun certificateChain(): List<X509Certificate>
    fun loadBody(): String                           // request body sent by the client
    fun headers(): Map<String, List<String>>         // ["H"] // value of "H" header
    fun cookies(): Map<String, Cookie>               // hash of browser cookies
    fun contentType(): String?                       // media type of request.body
    fun contentLength(): Long                        // length of request.body
}
