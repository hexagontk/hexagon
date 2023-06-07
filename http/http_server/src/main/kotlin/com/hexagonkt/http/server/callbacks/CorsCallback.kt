package com.hexagonkt.http.server.callbacks

import com.hexagonkt.core.Glob
import com.hexagonkt.http.model.*
import com.hexagonkt.http.model.HttpMethod.Companion.ALL
import com.hexagonkt.http.model.HttpMethod.OPTIONS
import com.hexagonkt.http.model.HttpStatusType.SUCCESS
import com.hexagonkt.http.handlers.HttpContext

/**
 * HTTP CORS callback. It holds info for CORS.
 */
class CorsCallback(
    private val allowedOrigin: Regex,
    private val allowedMethods: Set<HttpMethod> = ALL,
    private val allowedHeaders: Set<String> = emptySet(),
    private val exposedHeaders: Set<String> = emptySet(),
    private val supportCredentials: Boolean = true,
    private val preFlightStatus: HttpStatus = NO_CONTENT_204,
    private val preFlightMaxAge: Long = 0
) : (HttpContext) -> HttpContext {

    private companion object {
        const val ALLOW_ORIGIN = "access-control-allow-origin"
        const val ALLOW_CREDENTIALS = "access-control-allow-credentials"
        const val REQUEST_METHOD = "access-control-request-method"
        const val EXPOSE_HEADERS = "access-control-expose-headers"
        const val REQUEST_HEADERS = "access-control-request-headers"
        const val ALLOW_HEADERS = "access-control-allow-headers"
        const val MAX_AGE = "access-control-max-age"
    }

    constructor(
        allowedOrigin: String = "*",
        allowedMethods: Set<HttpMethod> = ALL,
        allowedHeaders: Set<String> = emptySet(),
        exposedHeaders: Set<String> = emptySet(),
        supportCredentials: Boolean = true,
        preFlightStatus: HttpStatus = NO_CONTENT_204,
        preFlightMaxAge: Long = 0) :
        this(
            Glob(allowedOrigin).regex,
            allowedMethods,
            allowedHeaders,
            exposedHeaders,
            supportCredentials,
            preFlightStatus,
            preFlightMaxAge
        )

    init {
        val preFlightStatusType = preFlightStatus.type

        require(preFlightStatusType == SUCCESS) {
            "Preflight Status must be a success status: $preFlightStatusType"
        }
    }

    override fun invoke(context: HttpContext): HttpContext =
        context.simpleRequest().let {
            if (context.request.method == OPTIONS) it.preFlightRequest()
            else it
        }.let {
            if (it.response.status != FORBIDDEN_403) it.next()
            else it
        }

    private fun allowOrigin(origin: String): Boolean =
        allowedOrigin.matches(origin)

    private fun accessControlAllowOrigin(origin: String): String =
        if (allowedOrigin.pattern == ".*" && !supportCredentials) "*"
        else origin

    private fun HttpContext.simpleRequest(): HttpContext {
        val origin = request.origin() ?: return this
        if (!allowOrigin(origin))
            return forbidden("Not allowed origin: $origin")

        val accessControlAllowOrigin = accessControlAllowOrigin(origin)
        var h = response.headers + Header(ALLOW_ORIGIN, accessControlAllowOrigin)

        if (accessControlAllowOrigin != "*")
            h += Header("vary", "Origin")

        if (supportCredentials)
            h += Header(ALLOW_CREDENTIALS, true)

        val accessControlRequestMethod = request.headers[REQUEST_METHOD]
        if (request.method == OPTIONS && accessControlRequestMethod != null)
            return badRequest()

        if (request.method !in allowedMethods)
            return forbidden("Not allowed method: ${request.method}")

        if (exposedHeaders.isNotEmpty()) {
            val requestHeaderNames = request.headers.httpFields.keys.toSet()
            val requestHeaders = requestHeaderNames.filter { it in exposedHeaders }

            h += Header(EXPOSE_HEADERS, requestHeaders.joinToString(","))
        }

        return send(preFlightStatus, headers = h)
    }

    private fun HttpContext.preFlightRequest(): HttpContext {

        val methodHeader = request.headers[REQUEST_METHOD]?.value as? String
        val requestMethod = methodHeader
            ?: return forbidden("$REQUEST_METHOD required header not found")

        val method = HttpMethod.valueOf(requestMethod)
        if (method !in allowedMethods)
            return forbidden("Not allowed method: $method")

        val accessControlRequestHeaders = request.headers[REQUEST_HEADERS]?.value as? String

        var h = response.headers

        if (accessControlRequestHeaders != null) {
            val allowedHeaders = accessControlRequestHeaders
                .split(",")
                .map { it.trim() }
                .all { it in allowedHeaders }

            if (!allowedHeaders && this@CorsCallback.allowedHeaders.isNotEmpty())
                return forbidden("Not allowed headers")

            val headers = this@CorsCallback.allowedHeaders
            val requestHeaders = headers.ifEmpty { request.headers.httpFields.keys.toSet() }
            h += Header(ALLOW_HEADERS, requestHeaders.joinToString(","))
        }

        h += Header(REQUEST_METHOD, allowedMethods.joinToString(","))

        if (preFlightMaxAge > 0)
            h += Header(MAX_AGE, preFlightMaxAge.toString())

        val origin = request.origin() ?: ""
        return when {
            allowOrigin(origin) && origin.isBlank() ->
                send(preFlightStatus, headers = h)
            allowOrigin(origin) ->
                send(preFlightStatus, headers = h + Header(ALLOW_ORIGIN, accessControlAllowOrigin(origin)))
            !allowOrigin(origin) && origin.isNotBlank() ->
                forbidden("Not allowed origin: $origin")
            else ->
                forbidden("Forbidden pre-flight request")
        }
    }
}
