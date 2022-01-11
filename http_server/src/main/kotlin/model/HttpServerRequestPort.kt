package com.hexagonkt.http.server.model

import com.hexagonkt.core.MultiMap
import com.hexagonkt.http.model.HttpRequest
import java.security.cert.X509Certificate

interface HttpServerRequestPort : HttpRequest {
    val certificateChain: List<X509Certificate>
    val contentLength: Long                        // length of request.body (or 0)
    val queryParameters: MultiMap<String, String>

    fun certificate(): X509Certificate? =
        certificateChain.firstOrNull()

    fun userAgent(): String? =
        headers["user-agent"]

    fun referer(): String? =
        headers["referer"]

    fun origin(): String? =
        headers["origin"]
}
