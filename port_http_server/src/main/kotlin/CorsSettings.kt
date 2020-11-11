package com.hexagonkt.http.server

import com.hexagonkt.helpers.globToRegex
import com.hexagonkt.http.ALL
import com.hexagonkt.http.Method

/**
 * HTTP CORS setting. It holds info for CORS.
 */
data class CorsSettings(
    val allowedOrigin: Regex,
    val allowedMethods: Set<Method> = ALL,
    val allowedHeaders: Set<String> = emptySet(),
    val exposedHeaders: Set<String> = emptySet(),
    val supportCredentials: Boolean = true,
    val preFlightStatus: Int = 204,
    val preFlightMaxAge: Long = 0
) {
    constructor(
        allowedOrigin: String = "*",
        allowedMethods: Set<Method> = ALL,
        allowedHeaders: Set<String> = emptySet(),
        exposedHeaders: Set<String> = emptySet(),
        supportCredentials: Boolean = true,
        preFlightStatus: Int = 204,
        preFlightMaxAge: Long = 0) :
            this(
                allowedOrigin.globToRegex(),
                allowedMethods,
                allowedHeaders,
                exposedHeaders,
                supportCredentials,
                preFlightStatus,
                preFlightMaxAge
            )

    /**
     * Checks whether given origin is allowed.
     *
     * @return True, if given origin is allowed, else false.
     */
    fun allowOrigin(origin: String): Boolean =
        allowedOrigin.matches(origin)

    /**
     * Provides the origin based on access control check for CORS.
     */
    fun accessControlAllowOrigin(origin: String): String =
        if (allowedOrigin.pattern == "^.*$" && !supportCredentials) "*"
        else origin
}
