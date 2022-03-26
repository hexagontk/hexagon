package com.hexagonkt.http.model

/**
 * TODO
 * Used in headers, query parameters and form parameters
 */
interface HttpField {
    val name: String
    val values: List<String>

    fun value(): String? =
        values.firstOrNull()

    operator fun plus(value: String)
}
