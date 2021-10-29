package com.hexagonkt.http.client

import com.hexagonkt.http.SslSettings
import com.hexagonkt.core.serialization.SerializationFormat

data class ClientSettings(
    val contentType: String? = null,
    val useCookies: Boolean = true,
    val headers: Map<String, List<String>> = LinkedHashMap(),
    val user: String? = null,
    val password: String? = null,
    val insecure: Boolean = false,
    val sslSettings: SslSettings? = null
) {

    constructor(
        format: SerializationFormat,
        useCookies: Boolean = true,
        headers: Map<String, List<String>> = LinkedHashMap(),
        user: String? = null,
        password: String? = null,
        insecure: Boolean = false):
            this(format.contentType, useCookies, headers, user, password, insecure)
}
