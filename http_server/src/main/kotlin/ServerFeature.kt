package com.hexagonkt.http.server

/**
 * Toolkit feature that may or may not be implemented by a server adapter.
 *
 * @property ZIP Request and response compression.
 * @property SESSIONS Data storage among request within a session.
 */
enum class ServerFeature {
    ZIP,
    SESSIONS
}
