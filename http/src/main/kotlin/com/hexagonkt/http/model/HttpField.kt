package com.hexagonkt.http.model

/**
 * TODO
 * Used in headers, query parameters and form parameters
 */
interface HttpField {
    val name: String
    val value: Any?
    val values: List<Any>

    fun valueString(): String? =
        value?.toString()

    operator fun plus(value: Any): HttpField

    operator fun minus(element: Any): HttpField
}
