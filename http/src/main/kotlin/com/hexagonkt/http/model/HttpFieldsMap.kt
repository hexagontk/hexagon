package com.hexagonkt.http.model

/**
 * TODO
 * Container for headers, query parameters and form parameters
 */
data class HttpFieldsMap(
    val httpFields: Map<String, HttpField>
)
