package com.hexagonkt.http.model

/**
 * Supported HTTP protocols.
 */
enum class HttpProtocol(val schema: String, val secure: Boolean) {
    /** HTTP. */
    HTTP("http", false),
    /** HTTPS. */
    HTTPS("https", true),
    /** HTTP/2. */
    HTTP2("https", true),
    /** HTTP/2 clear text. */
    H2C("http", false),
}
