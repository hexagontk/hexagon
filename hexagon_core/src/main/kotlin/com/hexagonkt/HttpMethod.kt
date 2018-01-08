package com.hexagonkt

/**
 * Enum for the supported HTTP methods.
 * TODO move to `http` subpackage
 * TODO move to `port_http`
 * TODO move `port_server` to `port_http_server` and `port_client` to `port_http_client`
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
    PATCH
}
