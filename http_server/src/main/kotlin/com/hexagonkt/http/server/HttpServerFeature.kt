package com.hexagonkt.http.server

/**
 * Toolkit feature that may or may not be implemented by a server adapter.
 *
 * @property ZIP Request and response compression.
 * @property ASYNC Asynchronous request processing.
 * @property WEB_SOCKETS Support for server Web Sockets.
 */
enum class HttpServerFeature {
    ZIP,
    ASYNC,
    WEB_SOCKETS,
}
