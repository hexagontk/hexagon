package com.hexagonkt.http.model

/**
 * TODO
 * Used in headers, query parameters and form parameters
 */
interface HttpField {
    val name: String
    val values: List<String>
    val value: String?

    operator fun plus(value: Any): HttpField

    operator fun minus(element: String): HttpField
}
