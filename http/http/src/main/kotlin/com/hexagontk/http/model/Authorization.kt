package com.hexagontk.http.model

data class Authorization(
    val type: String,
    val value: String,
) {
    val text: String by lazy { "$type $value" }
}
