package com.hexagontk.http.server.handlers

import com.hexagontk.http.handlers.FilterHandler
import com.hexagontk.http.handlers.HttpHandler
import com.hexagontk.http.handlers.HttpPredicate
import com.hexagontk.http.model.HttpMethod
import com.hexagontk.http.model.HttpMethod.Companion.ALL
import com.hexagontk.http.model.HttpStatus
import com.hexagontk.http.model.NO_CONTENT_204
import com.hexagontk.http.server.callbacks.CorsCallback

class CorsHandler(pattern: String, cors: CorsCallback) :
    HttpHandler by FilterHandler(HttpPredicate(pattern = pattern), cors) {

    constructor(cors: CorsCallback) : this("*", cors)

    constructor(
        pattern: String = "*",
        allowedOrigin: String = "*",
        allowedMethods: Set<HttpMethod> = ALL,
        allowedHeaders: Set<String> = emptySet(),
        exposedHeaders: Set<String> = emptySet(),
        supportCredentials: Boolean = true,
        preFlightStatus: HttpStatus = NO_CONTENT_204,
        preFlightMaxAge: Long = 0
    ) : this(
        pattern,
        CorsCallback(
            allowedOrigin,
            allowedMethods,
            allowedHeaders,
            exposedHeaders,
            supportCredentials,
            preFlightStatus,
            preFlightMaxAge,
        )
    )
}
