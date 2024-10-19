package com.hexagontk.http.model

class Authorization(
    val type: String,
    val value: String,
) : HttpValue {
    override val name: String = "authorization"
    override val text: String by lazy { "$type $value" }
}
