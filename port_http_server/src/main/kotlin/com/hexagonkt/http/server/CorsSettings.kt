package com.hexagonkt.http.server

import com.hexagonkt.helpers.globToRegex
import com.hexagonkt.http.ALL
import com.hexagonkt.http.Method

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

    fun allowOrigin(origin: String): Boolean =
        allowedOrigin.matches(origin)

    fun accessControlAllowOrigin(origin: String): String =
        if (allowedOrigin.pattern == "^.*$" && !supportCredentials) "*"
        else origin
}
