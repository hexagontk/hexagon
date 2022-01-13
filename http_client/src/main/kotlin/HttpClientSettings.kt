package com.hexagonkt.http.client

import com.hexagonkt.core.MultiMap
import com.hexagonkt.core.multiMapOf
import com.hexagonkt.http.SslSettings
import com.hexagonkt.http.model.ContentType
import java.net.URL

// TODO Add proxy configuration
data class HttpClientSettings(
    val baseUrl: URL = URL("http://localhost:2010"),
    val contentType: ContentType? = null,
    val useCookies: Boolean = true,
    val headers: MultiMap<String, String> = multiMapOf(),
    val insecure: Boolean = false,
    val sslSettings: SslSettings? = null
)
