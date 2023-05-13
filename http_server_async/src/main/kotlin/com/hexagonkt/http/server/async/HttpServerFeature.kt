package com.hexagonkt.http.server

/**
 * Toolkit feature that may or may not be implemented by a server adapter.
 *
 * @property ZIP Request and response compression.
 * @property WEB_SOCKETS Support for server Web Sockets.
 * @property SSE Support for Server Sent Events.
 */
enum class HttpServerFeature {
    ZIP,
    WEB_SOCKETS,
    SSE,
}
