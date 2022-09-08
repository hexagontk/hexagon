package com.hexagonkt.http.model

data class HttpAuthorization(
    val type: String,
    val value: String,
) {
    val text: String by lazy { "$type $value" }
}
