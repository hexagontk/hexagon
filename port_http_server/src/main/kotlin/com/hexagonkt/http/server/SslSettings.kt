package com.hexagonkt.http.server

import java.net.URI

data class SslSettings(
    val keyStore: URI? = null,
    val keyStorePassword: String? = null,
    val trustStore: URI? = null,
    val trustStorePassword: String? = null,
    val clientAuth: Boolean = false
)
