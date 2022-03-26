package com.hexagonkt.http.server

/**
 * Toolkit feature that may or may not be implemented by a server adapter.
 *
 * @property ZIP Request and response compression.
 * @property ASYNC Asynchronous request processing.
 */
enum class HttpServerFeature {
    ZIP,
    ASYNC,
}
