package com.hexagontk.http

import java.net.URL

class SslSettings(
    val keyStore: URL? = null,
    val keyStorePassword: String = "",
    val trustStore: URL? = null,
    val trustStorePassword: String = "",
    val clientAuth: Boolean = false
)
