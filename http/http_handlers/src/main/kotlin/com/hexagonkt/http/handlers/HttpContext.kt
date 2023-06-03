package com.hexagonkt.http.handlers

import com.hexagonkt.handlers.Context
import com.hexagonkt.core.assertEnabled
import com.hexagonkt.core.media.TEXT_EVENT_STREAM
import com.hexagonkt.core.media.TEXT_PLAIN
import com.hexagonkt.core.toText
import com.hexagonkt.handlers.Handler
import com.hexagonkt.http.model.*
import com.hexagonkt.http.model.INTERNAL_SERVER_ERROR_500
import com.hexagonkt.http.model.ServerEvent
import com.hexagonkt.http.model.ws.WsSession
import java.net.URL
import java.security.cert.X509Certificate
import java.util.concurrent.Flow.Publisher

// TODO Add exception parameter to 'send*' methods
data class HttpContext(
    override val event: HttpCall,
    override val predicate: (Context<HttpCall>) -> Boolean,
    override val nextHandlers: List<Handler<HttpCall>> = emptyList(),
    override val nextHandler: Int = 0,
    override val exception: Exception? = null,
    override val attributes: Map<*, *> = emptyMap<Any, Any>(),
): Context<HttpCall> {
    val request: HttpRequestPort = event.request
    val response: HttpResponsePort = event.response

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
        val httpHandler = predicate as HttpPredicate
        val pattern = httpHandler.pathPattern

        if (assertEnabled)
            check(!pattern.prefix) { "Loading path parameters not allowed for paths" }

        pattern.extractParameters(request.path)
    }

    constructor(context: Context<HttpCall>) : this(
        event = context.event,
        predicate = context.predicate,
        nextHandlers = context.nextHandlers,
        nextHandler = context.nextHandler,
        exception = context.exception,
        attributes = context.attributes,
    )

    constructor(
        request: HttpRequestPort = HttpRequest(),
        response: HttpResponsePort = HttpResponse(),
        predicate: HttpPredicate = HttpPredicate(),
        attributes: Map<*, *> = emptyMap<Any, Any>(),
    ) : this(HttpCall(request, response), predicate, attributes = attributes)

    override fun next(): HttpContext =
        super.next() as HttpContext

    override fun with(
        event: HttpCall,
        predicate: (Context<HttpCall>) -> Boolean,
        nextHandlers: List<Handler<HttpCall>>,
        nextHandler: Int,
        exception: Exception?,
        attributes: Map<*, *>
    ): Context<HttpCall> =
        copy(
            event = event,
            predicate = predicate,
            nextHandlers = nextHandlers,
            nextHandler = nextHandler,
            exception = exception,
            attributes = attributes,
        )

    fun unauthorized(
        body: Any = response.body,
        headers: Headers = response.headers,
        contentType: ContentType? = response.contentType,
        cookies: List<Cookie> = response.cookies,
        attributes: Map<*, *> = this.attributes,
    ): HttpContext =
        send(UNAUTHORIZED_401, body, headers, contentType, cookies, attributes)

    fun forbidden(
        body: Any = response.body,
        headers: Headers = response.headers,
        contentType: ContentType? = response.contentType,
        cookies: List<Cookie> = response.cookies,
        attributes: Map<*, *> = this.attributes,
    ): HttpContext =
        send(FORBIDDEN_403, body, headers, contentType, cookies, attributes)

    fun internalServerError(
        body: Any = response.body,
        headers: Headers = response.headers,
        contentType: ContentType? = response.contentType,
        cookies: List<Cookie> = response.cookies,
        attributes: Map<*, *> = this.attributes,
    ): HttpContext =
        send(INTERNAL_SERVER_ERROR_500, body, headers, contentType, cookies, attributes)

    fun serverError(
        status: HttpStatus,
        exception: Exception,
        headers: Headers = response.headers,
        attributes: Map<*, *> = this.attributes,
    ): HttpContext =
        send(
            status = status,
            body = exception.toText(),
            headers = headers,
            contentType = ContentType(TEXT_PLAIN),
            attributes = attributes,
        )

    fun internalServerError(
        exception: Exception,
        headers: Headers = response.headers,
        attributes: Map<*, *> = this.attributes,
    ): HttpContext =
        serverError(INTERNAL_SERVER_ERROR_500, exception, headers, attributes)

    fun ok(
        body: Any = response.body,
        headers: Headers = response.headers,
        contentType: ContentType? = response.contentType,
        cookies: List<Cookie> = response.cookies,
        attributes: Map<*, *> = this.attributes,
    ): HttpContext =
        send(OK_200, body, headers, contentType, cookies, attributes)

    fun sse(body: Publisher<ServerEvent>): HttpContext =
        ok(
            body = body,
            headers = response.headers + Header("cache-control", "no-cache"),
            contentType = ContentType(TEXT_EVENT_STREAM)
        )

    fun badRequest(
        body: Any = response.body,
        headers: Headers = response.headers,
        contentType: ContentType? = response.contentType,
        cookies: List<Cookie> = response.cookies,
        attributes: Map<*, *> = this.attributes,
    ): HttpContext =
        send(BAD_REQUEST_400, body, headers, contentType, cookies, attributes)

    fun notFound(
        body: Any = response.body,
        headers: Headers = response.headers,
        contentType: ContentType? = response.contentType,
        cookies: List<Cookie> = response.cookies,
        attributes: Map<*, *> = this.attributes,
    ): HttpContext =
        send(NOT_FOUND_404, body, headers, contentType, cookies, attributes)

    fun created(
        body: Any = response.body,
        headers: Headers = response.headers,
        contentType: ContentType? = response.contentType,
        cookies: List<Cookie> = response.cookies,
        attributes: Map<*, *> = this.attributes,
    ): HttpContext =
        send(CREATED_201, body, headers, contentType, cookies, attributes)

    fun accepted(
        onConnect: WsSession.() -> Unit = {},
        onBinary: WsSession.(data: ByteArray) -> Unit = {},
        onText: WsSession.(text: String) -> Unit = {},
        onPing: WsSession.(data: ByteArray) -> Unit = {},
        onPong: WsSession.(data: ByteArray) -> Unit = {},
        onClose: WsSession.(status: Int, reason: String) -> Unit = { _, _ -> },
    ): HttpContext =
        send(
            event.response.with(
                status = ACCEPTED_202,
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
        attributes: Map<*, *> = this.attributes,
    ): HttpContext =
        send(
            response.with(
                body = body,
                headers = headers,
                contentType = contentType,
                cookies = cookies,
                status = status,
            ),
            attributes
        )

    fun send(response: HttpResponsePort, attributes: Map<*, *> = this.attributes): HttpContext =
        copy(
            event = event.copy(response = response),
            attributes = attributes
        )

    fun send(request: HttpRequestPort, attributes: Map<*, *> = this.attributes): HttpContext =
        copy(
            event = event.copy(request = request),
            attributes = attributes
        )

    fun request(
        body: Any = response.body,
        headers: Headers = response.headers,
        contentType: ContentType? = response.contentType,
        cookies: List<Cookie> = response.cookies,
        attributes: Map<*, *> = this.attributes,
    ): HttpContext =
        send(
            request.with(
                body = body,
                headers = headers,
                contentType = contentType,
                cookies = cookies,
            ),
            attributes
        )
}
