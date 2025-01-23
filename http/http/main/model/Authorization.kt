package com.hexagontk.http.model

class Authorization(
    val type: String,
    val body: String,
) : HttpField {
    override val name: String = "authorization"
    override val value: String by lazy { text }

    override val text: String by lazy { "$type $body" }
}
