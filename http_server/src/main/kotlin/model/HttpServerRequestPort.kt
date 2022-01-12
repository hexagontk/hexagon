package com.hexagonkt.http.server.model

import com.hexagonkt.http.model.HttpRequest
import java.security.cert.X509Certificate

interface HttpServerRequestPort : HttpRequest {
    val certificateChain: List<X509Certificate>
    val contentLength: Long                        // length of request.body (or 0)

    fun certificate(): X509Certificate? =
        certificateChain.firstOrNull()
}
