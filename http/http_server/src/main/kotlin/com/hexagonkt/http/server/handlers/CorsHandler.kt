package com.hexagonkt.http.server.handlers

import com.hexagonkt.http.handlers.FilterHandler
import com.hexagonkt.http.handlers.HttpHandler
import com.hexagonkt.http.handlers.HttpPredicate
import com.hexagonkt.http.model.HttpMethod
import com.hexagonkt.http.model.HttpMethod.Companion.ALL
import com.hexagonkt.http.model.HttpStatus
import com.hexagonkt.http.model.NO_CONTENT_204
import com.hexagonkt.http.server.callbacks.CorsCallback

// TODO Write tests
class CorsHandler(pattern: String = "*", cors: CorsCallback = CorsCallback()) :
    HttpHandler by FilterHandler(HttpPredicate(pattern = pattern), cors) {

    constructor(cors: CorsCallback) : this("*", cors)

    constructor(
        pattern: String = "*",
        allowedOrigin: Regex,
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
