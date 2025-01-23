package com.hexagontk.http.model

/**
 * Supported HTTP methods.
 */
enum class HttpMethod {
    /** HTTP GET method. */
    GET,
    /** HTTP HEAD method. */
    HEAD,
    /** HTTP POST method. */
    POST,
    /** HTTP PUT method. */
    PUT,
    /** HTTP DELETE method. */
    DELETE,
    /** HTTP TRACE method. */
    TRACE,
    /** HTTP OPTIONS method. */
    OPTIONS,
    /** HTTP PATCH method. */
    PATCH;

    companion object {
        val ALL: Set<HttpMethod> = entries.toSet()
    }
}
