package com.hexagonkt.http.server

/**
 * Type of filter. Indicates when the filter is executed.
 */
enum class FilterOrder {
    /** Filter executed after the route is processed. */
    AFTER,
    /** Filter executed before the route is processed. */
    BEFORE
}
