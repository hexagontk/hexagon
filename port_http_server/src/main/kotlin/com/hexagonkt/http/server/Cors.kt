package com.hexagonkt.http.server

import com.hexagonkt.http.Method

internal fun Call.simpleRequest(settings: CorsSettings) {
    val origin = request.origin ?: return
    if (!settings.allowOrigin(origin))
        halt(403, "Not allowed origin: $origin")

    val accessControlAllowOrigin = settings.accessControlAllowOrigin(origin)
    response.setHeader("Access-Control-Allow-Origin", accessControlAllowOrigin)
    if (accessControlAllowOrigin != "*")
        response.setHeader("Vary", "Origin")

    if (settings.supportCredentials)
        response.setHeader("Access-Control-Allow-Credentials", true)

    val accessControlRequestMethod = request.headers["Access-Control-Request-Method"]?.first()
    if (request.method == Method.OPTIONS && accessControlRequestMethod != null)
        return

    if (request.method !in settings.allowedMethods)
        halt(403, "Not allowed method: ${request.method}")

    if (settings.exposedHeaders.isNotEmpty()) {
        val requestHeaderNames = request.headers.keys.toSet()
        val requestHeaders = requestHeaderNames.filter { it in settings.exposedHeaders }

        response.setHeader("Access-Control-Expose-Headers", requestHeaders.joinToString(","))
    }
}

internal fun Call.preFlightRequest(settings: CorsSettings) {

    val methodHeader = request.headers["Access-Control-Request-Method"]?.firstOrNull()
    val requestMethod = methodHeader
        ?: halt(403, "Access-Control-Request-Method required header not found")

    val method = Method.valueOf(requestMethod)
    if (method !in settings.allowedMethods)
        halt(403, "Not allowed method: $method")

    val accessControlRequestHeaders = request.headers["Access-Control-Request-Headers"]
        ?.firstOrNull()

    if (accessControlRequestHeaders != null) {
        val allowedHeaders = accessControlRequestHeaders
            .split(",")
            .map { it.trim() }
            .all { it in settings.allowedHeaders }

        if (!allowedHeaders && settings.allowedHeaders.isNotEmpty())
            halt(403, "Not allowed headers")

        val headers = settings.allowedHeaders
        val requestHeaders = if (headers.isEmpty()) request.headers.keys.toSet() else headers
        response.setHeader("Access-Control-Allow-Headers", requestHeaders.joinToString(","))
    }

    response.setHeader("Access-Control-Request-Method", settings.allowedMethods.joinToString(","))

    if (settings.preFlightMaxAge > 0)
        response.setHeader("Access-Control-Max-Age", settings.preFlightMaxAge)

    response.status = settings.preFlightStatus
}
