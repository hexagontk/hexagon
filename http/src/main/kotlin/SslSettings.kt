package com.hexagonkt.http

import java.net.URL

data class SslSettings(
    val keyStore: URL? = null,
    val keyStorePassword: String = "",
    val trustStore: URL? = null,
    val trustStorePassword: String = "",
    val clientAuth: Boolean = false
)
