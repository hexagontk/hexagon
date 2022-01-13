package com.hexagonkt.http.model

/**
 * Supported HTTP protocols.
 */
enum class HttpProtocol(val schema: String) {
    /** HTTP. */
    HTTP("http"),
    /** HTTPS. */
    HTTPS("https"),
    /** HTTP/2. */
    HTTP2("https"),
    /** Web Sockets. */
    WS("ws"),
    /** Secure Web Sockets. */
    WSS("wss"),
    /** HTTP/2 clear text. */
    H2C("http"),
}
