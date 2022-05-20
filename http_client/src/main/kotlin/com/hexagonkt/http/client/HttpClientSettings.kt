package com.hexagonkt.http.client

import com.hexagonkt.http.SslSettings
import com.hexagonkt.http.model.ContentType
import com.hexagonkt.http.model.Header
import com.hexagonkt.http.model.HttpFields
import java.net.URL

// TODO Add proxy configuration
data class HttpClientSettings(
    val baseUrl: URL = URL("http://localhost:2010"),
    val contentType: ContentType? = null,
    val useCookies: Boolean = true,
    val headers: HttpFields<Header> = HttpFields(),
    val insecure: Boolean = false,
    val sslSettings: SslSettings? = null
)
