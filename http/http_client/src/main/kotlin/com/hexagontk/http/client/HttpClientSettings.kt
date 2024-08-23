package com.hexagontk.http.client

import com.hexagontk.http.SslSettings
import com.hexagontk.http.model.*
import java.net.URL

// TODO Add proxy configuration and timeouts
data class HttpClientSettings(
    val baseUrl: URL? = null,
    val contentType: ContentType? = null,
    val accept: List<ContentType> = emptyList(),
    val useCookies: Boolean = true,
    val headers: Headers = Headers(),
    val insecure: Boolean = false,
    val sslSettings: SslSettings? = null,
    val authorization: Authorization? = null,
    val followRedirects: Boolean = false,
)
