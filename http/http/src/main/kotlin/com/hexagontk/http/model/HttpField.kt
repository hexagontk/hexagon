package com.hexagontk.http.model

/**
 * HTTP multi-value field. Used in headers, query parameters and form parameters.
 */
interface HttpField {
    val name: String
    val value: Any?
    val values: List<Any>

    fun string(): String? =
        value?.toString()

    fun strings(): List<String> =
        values.map(Any::toString)

    operator fun plus(value: Any): HttpField

    operator fun minus(element: Any): HttpField
}

// TODO Rename to HttpField, all HTTP fields must implement it (header, cookie, content-type,
//  authorization...)
internal interface HttpValue {
    val name: String
    val text: String
}

internal class HttpFields<T : HttpValue>(val d: List<T>) {
    val m: Map<String, List<T>> by lazy { d.groupBy { it.name } }
}
