package com.hexagonkt.http.server.handlers

import com.hexagonkt.core.handlers.Context
import com.hexagonkt.core.media.TextMedia
import com.hexagonkt.core.disableChecks
import com.hexagonkt.core.media.TextMedia.PLAIN
import com.hexagonkt.core.toText
import com.hexagonkt.http.model.*
import com.hexagonkt.http.model.ClientErrorStatus.*
import com.hexagonkt.http.model.ServerErrorStatus.INTERNAL_SERVER_ERROR
import com.hexagonkt.http.model.ServerEvent
import com.hexagonkt.http.model.SuccessStatus.*
import com.hexagonkt.http.server.model.*
import java.net.URL
import java.security.cert.X509Certificate
import java.util.concurrent.Flow.Publisher

// TODO Add exception parameter to 'send*' methods
data class HttpServerContext(val context: Context<HttpServerCall>) {
    val attributes: Map<*, *> = context.attributes
    val request: HttpServerRequestPort = context.event.request
    val response: HttpServerResponse = context.event.response
    val exception: Exception? = context.exception

    val method: HttpMethod by lazy { request.method }
    val protocol: HttpProtocol by lazy { request.protocol }
    val host: String by lazy { request.host }
    val port: Int by lazy { request.port }
    val path: String by lazy { request.path }
    val queryParameters: QueryParameters by lazy { request.queryParameters }
    val parts: List<HttpPart> by lazy { request.parts }
    val formParameters: FormParameters by lazy { request.formParameters }
    val accept: List<ContentType> by lazy { request.accept }
    val authorization: Authorization? by lazy { request.authorization }
    val certificateChain: List<X509Certificate> by lazy { request.certificateChain }

    val partsMap: Map<String, HttpPart> by lazy { request.partsMap() }
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

    constructor(
        request: HttpServerRequestPort = HttpServerRequest(),
        response: HttpServerResponse = HttpServerResponse(),
        predicate: HttpServerPredicate = HttpServerPredicate(),
        attributes: Map<*, *> = emptyMap<Any, Any>(),
    ) : this(Context(HttpServerCall(request, response), predicate, attributes = attributes))

    fun next(): HttpServerContext =
        HttpServerContext(context.next())

    fun success(
        status: SuccessStatus,
        body: Any = response.body,
        headers: Headers = response.headers,
        contentType: ContentType? = response.contentType,
        cookies: List<Cookie> = response.cookies,
        attributes: Map<*, *> = context.attributes,
    ): HttpServerContext =
        send(status, body, headers, contentType, cookies, attributes)

    fun redirect(
        status: RedirectionStatus,
        body: Any = response.body,
        headers: Headers = response.headers,
        contentType: ContentType? = response.contentType,
        cookies: List<Cookie> = response.cookies,
        attributes: Map<*, *> = context.attributes,
    ): HttpServerContext =
        send(status, body, headers, contentType, cookies, attributes)

    fun clientError(
        status: ClientErrorStatus,
        body: Any = response.body,
        headers: Headers = response.headers,
        contentType: ContentType? = response.contentType,
        cookies: List<Cookie> = response.cookies,
        attributes: Map<*, *> = context.attributes,
    ): HttpServerContext =
        send(status, body, headers, contentType, cookies, attributes)

    fun unauthorized(
        body: Any = response.body,
        headers: Headers = response.headers,
        contentType: ContentType? = response.contentType,
        cookies: List<Cookie> = response.cookies,
        attributes: Map<*, *> = context.attributes,
    ): HttpServerContext =
        send(UNAUTHORIZED, body, headers, contentType, cookies, attributes)

    fun forbidden(
        body: Any = response.body,
        headers: Headers = response.headers,
        contentType: ContentType? = response.contentType,
        cookies: List<Cookie> = response.cookies,
        attributes: Map<*, *> = context.attributes,
    ): HttpServerContext =
        send(FORBIDDEN, body, headers, contentType, cookies, attributes)

    fun serverError(
        status: ServerErrorStatus,
        body: Any = response.body,
        headers: Headers = response.headers,
        contentType: ContentType? = response.contentType,
        cookies: List<Cookie> = response.cookies,
        attributes: Map<*, *> = context.attributes,
    ): HttpServerContext =
        send(status, body, headers, contentType, cookies, attributes)

    fun internalServerError(
        body: Any = response.body,
        headers: Headers = response.headers,
        contentType: ContentType? = response.contentType,
        cookies: List<Cookie> = response.cookies,
        attributes: Map<*, *> = context.attributes,
    ): HttpServerContext =
        send(INTERNAL_SERVER_ERROR, body, headers, contentType, cookies, attributes)

    fun serverError(
        status: ServerErrorStatus,
        exception: Exception,
        headers: Headers = response.headers,
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
        headers: Headers = response.headers,
        attributes: Map<*, *> = context.attributes,
    ): HttpServerContext =
        serverError(INTERNAL_SERVER_ERROR, exception, headers, attributes)

    fun ok(
        body: Any = response.body,
        headers: Headers = response.headers,
        contentType: ContentType? = response.contentType,
        cookies: List<Cookie> = response.cookies,
        attributes: Map<*, *> = context.attributes,
    ): HttpServerContext =
        success(OK, body, headers, contentType, cookies, attributes)

    fun sse(body: Publisher<ServerEvent>): HttpServerContext =
        ok(
            body = body,
            headers = response.headers + Header("cache-control", "no-cache"),
            contentType = ContentType(TextMedia.EVENT_STREAM)
        )

    fun badRequest(
        body: Any = response.body,
        headers: Headers = response.headers,
        contentType: ContentType? = response.contentType,
        cookies: List<Cookie> = response.cookies,
        attributes: Map<*, *> = context.attributes,
    ): HttpServerContext =
        clientError(BAD_REQUEST, body, headers, contentType, cookies, attributes)

    fun notFound(
        body: Any = response.body,
        headers: Headers = response.headers,
        contentType: ContentType? = response.contentType,
        cookies: List<Cookie> = response.cookies,
        attributes: Map<*, *> = context.attributes,
    ): HttpServerContext =
        clientError(NOT_FOUND, body, headers, contentType, cookies, attributes)

    fun created(
        body: Any = response.body,
        headers: Headers = response.headers,
        contentType: ContentType? = response.contentType,
        cookies: List<Cookie> = response.cookies,
        attributes: Map<*, *> = context.attributes,
    ): HttpServerContext =
        success(CREATED, body, headers, contentType, cookies, attributes)

    fun accepted(
        onConnect: WsSession.() -> Unit = {},
        onBinary: WsSession.(data: ByteArray) -> Unit = {},
        onText: WsSession.(text: String) -> Unit = {},
        onPing: WsSession.(data: ByteArray) -> Unit = {},
        onPong: WsSession.(data: ByteArray) -> Unit = {},
        onClose: WsSession.(statusCode: Int, reason: String) -> Unit = { _, _ -> },
    ): HttpServerContext =
        send(
            context.event.response.copy(
                status = ACCEPTED,
                onConnect = onConnect,
                onBinary = onBinary,
                onText = onText,
                onPing = onPing,
                onPong = onPong,
                onClose = onClose,
            )
        )

    fun send(
        status: HttpStatus = response.status,
        body: Any = response.body,
        headers: Headers = response.headers,
        contentType: ContentType? = response.contentType,
        cookies: List<Cookie> = response.cookies,
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
        response: HttpServerResponse, attributes: Map<*, *> = context.attributes
    ): HttpServerContext =
        HttpServerContext(
            context.copy(
                event = context.event.copy(response = response),
                attributes = attributes
            )
        )
}
