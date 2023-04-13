package com.hexagonkt.http.client

import com.hexagonkt.http.SslSettings
import com.hexagonkt.http.model.*
import java.net.URL

// TODO Add proxy configuration
data class HttpClientSettings(
    val baseUrl: URL? = null,
    val contentType: ContentType? = null,
    val useCookies: Boolean = true,
    val headers: Headers = Headers(),
    val insecure: Boolean = false,
    val sslSettings: SslSettings? = null,
    val authorization: Authorization? = null,
)
