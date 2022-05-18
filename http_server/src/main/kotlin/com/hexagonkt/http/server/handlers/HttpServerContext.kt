package com.hexagonkt.http.server.handlers

import com.hexagonkt.core.handlers.Context
import com.hexagonkt.core.MultiMap
import com.hexagonkt.core.media.TextMedia
import com.hexagonkt.core.disableChecks
import com.hexagonkt.core.media.TextMedia.PLAIN
import com.hexagonkt.core.toText
import com.hexagonkt.http.model.*
import com.hexagonkt.http.model.ClientErrorStatus.BAD_REQUEST
import com.hexagonkt.http.model.ClientErrorStatus.NOT_FOUND
import com.hexagonkt.http.model.ServerErrorStatus.INTERNAL_SERVER_ERROR
import com.hexagonkt.http.model.HttpServerEvent
import com.hexagonkt.http.model.SuccessStatus.CREATED
import com.hexagonkt.http.model.SuccessStatus.OK
import com.hexagonkt.http.server.model.HttpServerCall
import com.hexagonkt.http.server.model.HttpServerRequestPort
import com.hexagonkt.http.server.model.HttpServerResponse
import java.net.URL
import java.security.cert.X509Certificate
import java.util.concurrent.SubmissionPublisher

// TODO Add exception parameter to 'send*' methods
data class HttpServerContext(
    val context: Context<HttpServerCall>,
    val attributes: Map<*, *> = context.attributes
) {
    val request: HttpServerRequestPort = context.event.request
    val response: HttpServerResponse = context.event.response
    val exception: Exception? = context.exception

    val method: HttpMethod by lazy { request.method }
    val protocol: HttpProtocol by lazy { request.protocol }
    val host: String by lazy { request.host }
    val port: Int by lazy { request.port }
    val path: String by lazy { request.path }
    val queryParameters: HttpFields<QueryParameter> by lazy { request.queryParameters }
    val parts: List<HttpPartPort> by lazy { request.parts }
    val formParameters: HttpFields<FormParameter> by lazy { request.formParameters }
    val accept: List<ContentType> by lazy { request.accept }
    val certificateChain: List<X509Certificate> by lazy { request.certificateChain }

    val partsMap: Map<String, HttpPartPort> by lazy { request.partsMap() }
    val url: URL by lazy { request.url() }
    val userAgent: String? by lazy { request.userAgent() }
    val referer: String? by lazy { request.referer() }
    val origin: String? by lazy { request.origin() }
    val certificate: X509Certificate? by lazy { request.certificate() }

    val status: HttpStatus = response.status

    val pathParameters: Map<String, String> by lazy {
        val httpHandler = context.currentFilter as HttpServerPredicate
        val pattern = httpHandler.pathPattern

        if (!disableChecks)
            check(!(pattern.prefix)) { "Loading path parameters not allowed for paths" }

        pattern.extractParameters(request.path)
    }

    val allParameters: MultiMap<String, *> by lazy {
        MultiMap(
            request.formParameters.allValues
                + request.queryParameters.allValues
                + pathParameters.mapValues { listOf(it.value) }
        )
    }

    fun next(): HttpServerContext =
        HttpServerContext(context.next())

    fun success(
        status: SuccessStatus,
        body: Any = response.body,
        headers: MultiMap<String, String> = response.headers,
        contentType: ContentType? = response.contentType,
        cookies: List<HttpCookie> = response.cookies,
        attributes: Map<*, *> = context.attributes,
    ): HttpServerContext =
        send(status, body, headers, contentType, cookies, attributes)

    fun redirect(
        status: RedirectionStatus,
        body: Any = response.body,
        headers: MultiMap<String, String> = response.headers,
        contentType: ContentType? = response.contentType,
        cookies: List<HttpCookie> = response.cookies,
        attributes: Map<*, *> = context.attributes,
    ): HttpServerContext =
        send(status, body, headers, contentType, cookies, attributes)

    fun clientError(
        status: ClientErrorStatus,
        body: Any = response.body,
        headers: MultiMap<String, String> = response.headers,
        contentType: ContentType? = response.contentType,
        cookies: List<HttpCookie> = response.cookies,
        attributes: Map<*, *> = context.attributes,
    ): HttpServerContext =
        send(status, body, headers, contentType, cookies, attributes)

    fun serverError(
        status: ServerErrorStatus,
        body: Any = response.body,
        headers: MultiMap<String, String> = response.headers,
        contentType: ContentType? = response.contentType,
        cookies: List<HttpCookie> = response.cookies,
        attributes: Map<*, *> = context.attributes,
    ): HttpServerContext =
        send(status, body, headers, contentType, cookies, attributes)

    fun internalServerError(
        body: Any = response.body,
        headers: MultiMap<String, String> = response.headers,
        contentType: ContentType? = response.contentType,
        cookies: List<HttpCookie> = response.cookies,
        attributes: Map<*, *> = context.attributes,
    ): HttpServerContext =
        send(INTERNAL_SERVER_ERROR, body, headers, contentType, cookies, attributes)

    fun serverError(
        status: ServerErrorStatus,
        exception: Exception,
        headers: MultiMap<String, String> = response.headers,
        attributes: Map<*, *> = context.attributes,
    ): HttpServerContext =
        serverError(
            status = status,
            body = exception.toText(),
            headers = headers,
            contentType = ContentType(PLAIN),
            attributes = attributes,
        )

    fun internalServerError(
        exception: Exception,
        headers: MultiMap<String, String> = response.headers,
        attributes: Map<*, *> = context.attributes,
    ): HttpServerContext =
        serverError(INTERNAL_SERVER_ERROR, exception, headers, attributes)

    fun ok(
        body: Any = response.body,
        headers: MultiMap<String, String> = response.headers,
        contentType: ContentType? = response.contentType,
        cookies: List<HttpCookie> = response.cookies,
        attributes: Map<*, *> = context.attributes,
    ): HttpServerContext =
        success(OK, body, headers, contentType, cookies, attributes)

    fun sse(body: SubmissionPublisher<HttpServerEvent>): HttpServerContext =
        ok(
            body = body,
            headers = response.headers + ("cache-control" to "no-cache"),
            contentType = ContentType(TextMedia.EVENT_STREAM)
        )

    fun badRequest(
        body: Any = response.body,
        headers: MultiMap<String, String> = response.headers,
        contentType: ContentType? = response.contentType,
        cookies: List<HttpCookie> = response.cookies,
        attributes: Map<*, *> = context.attributes,
    ): HttpServerContext =
        clientError(BAD_REQUEST, body, headers, contentType, cookies, attributes)

    fun notFound(
        body: Any = response.body,
        headers: MultiMap<String, String> = response.headers,
        contentType: ContentType? = response.contentType,
        cookies: List<HttpCookie> = response.cookies,
        attributes: Map<*, *> = context.attributes,
    ): HttpServerContext =
        clientError(NOT_FOUND, body, headers, contentType, cookies, attributes)

    fun created(
        body: Any = response.body,
        headers: MultiMap<String, String> = response.headers,
        contentType: ContentType? = response.contentType,
        cookies: List<HttpCookie> = response.cookies,
        attributes: Map<*, *> = context.attributes,
    ): HttpServerContext =
        success(CREATED, body, headers, contentType, cookies, attributes)

    fun send(
        status: HttpStatus = response.status,
        body: Any = response.body,
        headers: MultiMap<String, String> = response.headers,
        contentType: ContentType? = response.contentType,
        cookies: List<HttpCookie> = response.cookies,
        attributes: Map<*, *> = context.attributes,
    ): HttpServerContext =
        send(
            response.copy(
                body = body,
                headers = headers,
                contentType = contentType,
                cookies = cookies,
                status = status,
            ),
            attributes
        )

    fun send(
        response: HttpServerResponse, attributes: Map<*, *> = emptyMap<Any, Any>()
    ): HttpServerContext =
        HttpServerContext(
            context.copy(
                event = context.event.copy(response = response),
                attributes = attributes
            )
        )
}
