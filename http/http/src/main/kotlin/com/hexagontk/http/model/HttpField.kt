package com.hexagontk.http.model

/**
 * HTTP field single-value. Used in headers, query parameters and form parameters.
 */
interface HttpField {
    val name: String
    val value: Any?
    val text: String
}
