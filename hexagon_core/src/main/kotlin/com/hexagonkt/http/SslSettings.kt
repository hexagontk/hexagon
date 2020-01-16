package com.hexagonkt.http

import java.net.URI

data class SslSettings(
    val keyStore: URI? = null,
    val trustStore: URI? = null,
    val clientAuth: Boolean = false
)
