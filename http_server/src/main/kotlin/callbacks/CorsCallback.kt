package com.hexagonkt.http.server.callbacks

import com.hexagonkt.core.helpers.Glob
import com.hexagonkt.http.model.ClientErrorStatus.FORBIDDEN
import com.hexagonkt.http.model.HttpMethod
import com.hexagonkt.http.model.HttpMethod.Companion.ALL
import com.hexagonkt.http.model.HttpMethod.OPTIONS
import com.hexagonkt.http.model.SuccessStatus
import com.hexagonkt.http.server.handlers.HttpCallback
import com.hexagonkt.http.server.handlers.HttpServerContext
import com.hexagonkt.http.model.SuccessStatus.NO_CONTENT

/**
 * HTTP CORS callback. It holds info for CORS.
 */
class CorsCallback(
    private val allowedOrigin: Regex,
    private val allowedMethods: Set<HttpMethod> = ALL,
    private val allowedHeaders: Set<String> = emptySet(),
    private val exposedHeaders: Set<String> = emptySet(),
    private val supportCredentials: Boolean = true,
    private val preFlightStatus: SuccessStatus = NO_CONTENT,
    private val preFlightMaxAge: Long = 0
) : HttpCallback {

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
        preFlightStatus: SuccessStatus = NO_CONTENT,
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

    override suspend fun invoke(context: HttpServerContext): HttpServerContext =
        context.simpleRequest().let {
            if (context.request.method == OPTIONS) it.preFlightRequest()
            else it
        }.let {
            if (it.response.status != FORBIDDEN) it.next()
            else it
        }

    private fun allowOrigin(origin: String): Boolean =
        allowedOrigin.matches(origin)

    private fun accessControlAllowOrigin(origin: String): String =
        if (allowedOrigin.pattern == ".*" && !supportCredentials) "*"
        else origin

    private fun HttpServerContext.simpleRequest(): HttpServerContext {
        val origin = request.origin() ?: return this
        if (!allowOrigin(origin))
            return clientError(FORBIDDEN, "Not allowed origin: $origin")

        val accessControlAllowOrigin = accessControlAllowOrigin(origin)
        var h = response.headers + (ALLOW_ORIGIN to accessControlAllowOrigin)

        if (accessControlAllowOrigin != "*")
            h += "vary" to "Origin"

        if (supportCredentials)
            h += ALLOW_CREDENTIALS to true.toString()

        val accessControlRequestMethod = request.headers[REQUEST_METHOD]
        if (request.method == OPTIONS && accessControlRequestMethod != null)
            return badRequest()

        if (request.method !in allowedMethods)
            return clientError(FORBIDDEN, "Not allowed method: ${request.method}")

        if (exposedHeaders.isNotEmpty()) {
            val requestHeaderNames = request.headers.keys.toSet()
            val requestHeaders = requestHeaderNames.filter { it in exposedHeaders }

            h += EXPOSE_HEADERS to requestHeaders.joinToString(",")
        }

        return success(preFlightStatus, headers = h)
    }

    private fun HttpServerContext.preFlightRequest(): HttpServerContext {

        val methodHeader = request.headers[REQUEST_METHOD]
        val requestMethod = methodHeader
            ?: return clientError(FORBIDDEN, "$REQUEST_METHOD required header not found")

        val method = HttpMethod.valueOf(requestMethod)
        if (method !in allowedMethods)
            return clientError(FORBIDDEN, "Not allowed method: $method")

        val accessControlRequestHeaders = request.headers[REQUEST_HEADERS]

        var h = response.headers

        if (accessControlRequestHeaders != null) {
            val allowedHeaders = accessControlRequestHeaders
                .split(",")
                .map { it.trim() }
                .all { it in allowedHeaders }

            if (!allowedHeaders && this@CorsCallback.allowedHeaders.isNotEmpty())
                return clientError(FORBIDDEN, "Not allowed headers")

            val headers = this@CorsCallback.allowedHeaders
            val requestHeaders = headers.ifEmpty { request.headers.keys.toSet() }
            h += ALLOW_HEADERS to requestHeaders.joinToString(",")
        }

        h += REQUEST_METHOD to allowedMethods.joinToString(",")

        if (preFlightMaxAge > 0)
            h += MAX_AGE to preFlightMaxAge.toString()

        return success(preFlightStatus, headers = h)
    }
}
