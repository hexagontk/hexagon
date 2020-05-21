package com.hexagonkt.http

import java.net.URI

data class SslSettings(
    val keyStore: URI? = null,
    val keyStorePassword: String = "",
    val trustStore: URI? = null,
    val trustStorePassword: String = "",
    val clientAuth: Boolean = false
)
