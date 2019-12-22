package com.hexagonkt.http.server

import java.net.URI

data class SslSettings(
    val keyStore: URI? = null,
    val trustStore: URI? = null,
    val clientAuth: Boolean = false
)
