package com.hexagontk.http

/**
 * Toolkit feature that may or may not be implemented by a server adapter.
 *
 * @property ZIP Request and response compression.
 * @property WEBSOCKETS Support for server Web Sockets.
 * @property SSE Support for Server Sent Events.
 *
 * TODO To be used in clients/servers (take advantage in tests to fire only tests that apply)
 */
enum class HttpFeature {
    ZIP,
    COOKIES,
    MULTIPART,
    WEBSOCKETS,
    SSE,
}
