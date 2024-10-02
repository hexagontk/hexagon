package com.hexagontk.http

/**
 * Toolkit feature that may or may not be implemented by a server adapter.
 *
 * TODO Take advantage in tests to fire only tests that apply
 *
 * @property ZIP Request and response compression.
 * @property COOKIES .
 * @property MULTIPART .
 * @property WEBSOCKETS Support for server Web Sockets.
 * @property SSE Support for Server Sent Events.
 */
enum class HttpFeature {
    ZIP,
    COOKIES,
    MULTIPART,
    WEBSOCKETS,
    SSE,
}
