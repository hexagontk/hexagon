package com.hexagonkt.http.server

import com.hexagonkt.http.Method

private const val ALLOW_ORIGIN = "Access-Control-Allow-Origin"
private const val ALLOW_CREDENTIALS = "Access-Control-Allow-Credentials"
private const val REQUEST_METHOD = "Access-Control-Request-Method"
private const val EXPOSE_HEADERS = "Access-Control-Expose-Headers"
private const val REQUEST_HEADERS = "Access-Control-Request-Headers"
private const val ALLOW_HEADERS = "Access-Control-Allow-Headers"
private const val MAX_AGE = "Access-Control-Max-Age"

internal fun Call.simpleRequest(settings: CorsSettings) {
    val origin = request.origin ?: return
    if (!settings.allowOrigin(origin))
        halt(403, "Not allowed origin: $origin")

    val accessControlAllowOrigin = settings.accessControlAllowOrigin(origin)
    response.setHeader(ALLOW_ORIGIN, accessControlAllowOrigin)
    if (accessControlAllowOrigin != "*")
        response.setHeader("Vary", "Origin")

    if (settings.supportCredentials)
        response.setHeader(ALLOW_CREDENTIALS, true)

    val accessControlRequestMethod = request.headers[REQUEST_METHOD]?.first()
    if (request.method == Method.OPTIONS && accessControlRequestMethod != null)
        return

    if (request.method !in settings.allowedMethods)
        halt(403, "Not allowed method: ${request.method}")

    if (settings.exposedHeaders.isNotEmpty()) {
        val requestHeaderNames = request.headers.keys.toSet()
        val requestHeaders = requestHeaderNames.filter { it in settings.exposedHeaders }

        response.setHeader(EXPOSE_HEADERS, requestHeaders.joinToString(","))
    }
}

internal fun Call.preFlightRequest(settings: CorsSettings) {

    val methodHeader = request.headers[REQUEST_METHOD]?.firstOrNull()
    val requestMethod = methodHeader
        ?: halt(403, "Access-Control-Request-Method required header not found")

    val method = Method.valueOf(requestMethod)
    if (method !in settings.allowedMethods)
        halt(403, "Not allowed method: $method")

    val accessControlRequestHeaders = request.headers[REQUEST_HEADERS]
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
        response.setHeader(ALLOW_HEADERS, requestHeaders.joinToString(","))
    }

    response.setHeader(REQUEST_METHOD, settings.allowedMethods.joinToString(","))

    if (settings.preFlightMaxAge > 0)
        response.setHeader(MAX_AGE, settings.preFlightMaxAge)

    response.status = settings.preFlightStatus
}
