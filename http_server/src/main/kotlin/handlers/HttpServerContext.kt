package com.hexagonkt.http.server.handlers

import com.hexagonkt.core.handlers.Context
import com.hexagonkt.core.helpers.MultiMap
import com.hexagonkt.core.media.TextMedia
import com.hexagonkt.core.disableChecks
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
import kotlinx.coroutines.flow.Flow

data class HttpServerContext(
    val context: Context<HttpServerCall>,
    val request: HttpServerRequestPort = context.event.request,
    val response: HttpServerResponse = context.event.response,
    val attributes: Map<Any, Any> = context.attributes
) {

    val pathParameters: Map<String, String> by lazy {
        val httpHandler = context.currentFilter as HttpServerPredicate
        val pattern = httpHandler.pathPattern

        if (!disableChecks)
            check(!(pattern.prefix)) { "Loading path parameters not allowed for paths" }

        pattern.extractParameters(request.path)
    }

    val allParameters: Map<String, Any> by lazy {
        request.formParameters.allValues + request.queryParameters.allValues + pathParameters
    }

    suspend fun next(): HttpServerContext =
        HttpServerContext(context.next())

    fun success(
        status: SuccessStatus,
        body: Any = response.body,
        headers: MultiMap<String, String> = response.headers,
        contentType: ContentType? = response.contentType,
        cookies: List<HttpCookie> = response.cookies,
        attributes: Map<Any, Any> = context.attributes,
    ): HttpServerContext =
        send(status, body, headers, contentType, cookies, attributes)

    fun redirect(
        status: RedirectionStatus,
        body: Any = response.body,
        headers: MultiMap<String, String> = response.headers,
        contentType: ContentType? = response.contentType,
        cookies: List<HttpCookie> = response.cookies,
        attributes: Map<Any, Any> = context.attributes,
    ): HttpServerContext =
        send(status, body, headers, contentType, cookies, attributes)

    fun clientError(
        status: ClientErrorStatus,
        body: Any = response.body,
        headers: MultiMap<String, String> = response.headers,
        contentType: ContentType? = response.contentType,
        cookies: List<HttpCookie> = response.cookies,
        attributes: Map<Any, Any> = context.attributes,
    ): HttpServerContext =
        send(status, body, headers, contentType, cookies, attributes)

    fun serverError(
        status: ServerErrorStatus,
        body: Any = response.body,
        headers: MultiMap<String, String> = response.headers,
        contentType: ContentType? = response.contentType,
        cookies: List<HttpCookie> = response.cookies,
        attributes: Map<Any, Any> = context.attributes,
    ): HttpServerContext =
        send(status, body, headers, contentType, cookies, attributes)

    fun internalServerError(
        body: Any = response.body,
        headers: MultiMap<String, String> = response.headers,
        contentType: ContentType? = response.contentType,
        cookies: List<HttpCookie> = response.cookies,
        attributes: Map<Any, Any> = context.attributes,
    ): HttpServerContext =
        send(INTERNAL_SERVER_ERROR, body, headers, contentType, cookies, attributes)

    fun ok(
        body: Any = response.body,
        headers: MultiMap<String, String> = response.headers,
        contentType: ContentType? = response.contentType,
        cookies: List<HttpCookie> = response.cookies,
        attributes: Map<Any, Any> = context.attributes,
    ): HttpServerContext =
        success(OK, body, headers, contentType, cookies, attributes)

    fun sse(body: Flow<HttpServerEvent>): HttpServerContext =
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
        attributes: Map<Any, Any> = context.attributes,
    ): HttpServerContext =
        clientError(BAD_REQUEST, body, headers, contentType, cookies, attributes)

    fun notFound(
        body: Any = response.body,
        headers: MultiMap<String, String> = response.headers,
        contentType: ContentType? = response.contentType,
        cookies: List<HttpCookie> = response.cookies,
        attributes: Map<Any, Any> = context.attributes,
    ): HttpServerContext =
        clientError(NOT_FOUND, body, headers, contentType, cookies, attributes)

    fun created(
        body: Any = response.body,
        headers: MultiMap<String, String> = response.headers,
        contentType: ContentType? = response.contentType,
        cookies: List<HttpCookie> = response.cookies,
        attributes: Map<Any, Any> = context.attributes,
    ): HttpServerContext =
        success(CREATED, body, headers, contentType, cookies, attributes)

    fun send(
        status: HttpStatus = response.status,
        body: Any = response.body,
        headers: MultiMap<String, String> = response.headers,
        contentType: ContentType? = response.contentType,
        cookies: List<HttpCookie> = response.cookies,
        attributes: Map<Any, Any> = context.attributes,
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
        response: HttpServerResponse, attributes: Map<Any, Any> = emptyMap()): HttpServerContext =
        HttpServerContext(
            context.copy(
                event = context.event.copy(response = response),
                attributes = attributes
            )
        )
}
