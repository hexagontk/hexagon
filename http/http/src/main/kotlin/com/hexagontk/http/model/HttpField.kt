package com.hexagontk.http.model

/**
 * HTTP field single-value. Used in headers, query parameters and form parameters.
 *
 * TODO Use HttpFieldPort and rename Header to HttpField
 */
interface HttpField {
    val name: String
    val value: Any?
    val text: String
}
