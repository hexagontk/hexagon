package com.hexagonkt.rest.tools

import com.hexagonkt.core.media.MediaType
import com.hexagonkt.core.urlOf
import com.hexagonkt.http.SslSettings
import com.hexagonkt.http.client.HttpClient
import com.hexagonkt.http.client.HttpClientPort
import com.hexagonkt.http.client.HttpClientSettings
import com.hexagonkt.http.handlers.BeforeHandler
import com.hexagonkt.http.handlers.HttpHandler
import com.hexagonkt.http.model.HttpRequest
import com.hexagonkt.http.model.*
import com.hexagonkt.http.model.HttpMethod.*
import com.hexagonkt.http.model.HttpStatusType.SUCCESS
import com.hexagonkt.http.patterns.createPathPattern
import com.hexagonkt.rest.SerializeRequestCallback

data class Http(
    val adapter: HttpClientPort,
    val url: String? = null,
    val httpContentType: ContentType? = null,
    val httpAccept: List<ContentType> = emptyList(),
    val httpHeaders: Map<String, *> = emptyMap<String, Any>(),
    val sslSettings: SslSettings? = SslSettings(),
    val handler: HttpHandler? = serializeHandler,
) {
    companion object {
        val serializeHandler: HttpHandler = BeforeHandler("*", SerializeRequestCallback())
    }

    private val settings =
        HttpClientSettings(
            baseUrl = url?.let(::urlOf),
            contentType = httpContentType,
            accept = httpAccept,
            useCookies = true,
            headers = toHeaders(httpHeaders),
            insecure = true,
            sslSettings = sslSettings,
        )

    private val client = HttpClient(adapter, settings, handler = handler)
    private lateinit var lastRequest: HttpRequest
    private lateinit var lastAttributes: Map<String, *>
    private lateinit var lastResponse: HttpResponsePort
    val request: HttpRequest get() = lastRequest
    val attributes: Map<String, *> get() = lastAttributes
    val response: HttpResponsePort get() = lastResponse
    val status: HttpStatus get() = lastResponse.status
    val body: Any get() = lastResponse.body
    val cookies: Map<String, Cookie> get() = lastResponse.cookiesMap()
    val headers: Headers get() = lastResponse.headers
    val contentType: ContentType? get() = lastResponse.contentType

    constructor(
        adapter: HttpClientPort,
        url: String? = null,
        mediaType: MediaType,
        accept: List<MediaType> = emptyList(),
        headers: Map<String, *> = emptyMap<String, Any>(),
        sslSettings: SslSettings? = SslSettings(),
        handler: HttpHandler? = serializeHandler,
    ) : this(
        adapter,
        url,
        ContentType(mediaType),
        accept.map(::ContentType),
        headers,
        sslSettings,
        handler
    )

    fun start() {
        if (!client.started())
            client.start()
    }

    fun stop() {
        if (client.started())
            client.stop()
    }

    fun request(block: Http.() -> Unit) {
        client.request { block.invoke(this@Http) }
    }

    fun assertStatus(status: HttpStatus) {
        assert(status == lastResponse.status)
    }

    fun assertOk() {
        assertStatus(OK_200)
    }

    fun assertStatus(statusType: HttpStatusType) {
        assert(statusType == lastResponse.status.type)
    }

    fun assertSuccess() {
        assertStatus(SUCCESS)
    }

    fun assertBody(body: Any) {
        assert(body == lastResponse.body)
    }

    fun assertBodyContains(vararg texts: String) {
        texts.forEach { assert(lastResponse.bodyString().contains(it)) }
    }

    private fun toHeaders(map: Map<String, *>): Headers = Headers(
        map.mapValues { (k, v) ->
            Header(
                k,
                when (v) {
                    is Collection<*> -> v.map { it.toString() }.toList()
                    else -> listOf(v.toString())
                }
            )
        }
    )

    private fun send(
        method: HttpMethod = GET,
        path: String = "/",
        headers: Map<String, *> = emptyMap<String, Any>(),
        body: Any = "",
        formParameters: List<FormParameter> = emptyList(),
        parts: List<HttpPart> = emptyList(),
        contentType: ContentType? = settings.contentType,
        accept: List<ContentType> = settings.accept,
        attributes: Map<String, Any> = emptyMap(),
    ): HttpResponsePort =
        client
            .apply {
                if (!started())
                    start()

                lastAttributes = attributes
                lastRequest = HttpRequest(
                    method = method,
                    path = path,
                    body = body,
                    headers = toHeaders(headers),
                    formParameters = FormParameters(formParameters),
                    parts = parts,
                    contentType = contentType,
                    accept = accept,
                )
            }
            .send(lastRequest, attributes = attributes)
            .apply { lastResponse = this }

    private fun send(
        method: HttpMethod = GET,
        pathPattern: String,
        pathParameters: Map<String, Any>,
        headers: Map<String, *> = emptyMap<String, Any>(),
        body: Any = "",
        formParameters: List<FormParameter> = emptyList(),
        parts: List<HttpPart> = emptyList(),
        contentType: ContentType? = settings.contentType,
        accept: List<ContentType> = settings.accept,
        attributes: Map<String, Any> = emptyMap(),
    ): HttpResponsePort =
        send(
            method = method,
            path = createPathPattern(pathPattern, false).insertParameters(pathParameters),
            headers = toHeaders(headers),
            body = body,
            formParameters = formParameters,
            parts = parts,
            contentType = contentType,
            accept = accept,
            attributes = attributes
                + mapOf("pathPattern" to pathPattern, "pathParameters" to pathParameters),
        )

    fun get(
        path: String = "/",
        headers: Map<String, *> = emptyMap<String, Any>(),
        body: Any = "",
        formParameters: List<FormParameter> = emptyList(),
        parts: List<HttpPart> = emptyList(),
        contentType: ContentType? = settings.contentType,
        accept: List<ContentType> = settings.accept,
    ): HttpResponsePort =
        send(GET, path, headers, body, formParameters, parts, contentType, accept)

    fun put(
        path: String = "/",
        body: Any = "",
        headers: Map<String, *> = emptyMap<String, Any>(),
        formParameters: List<FormParameter> = emptyList(),
        parts: List<HttpPart> = emptyList(),
        contentType: ContentType? = settings.contentType,
        accept: List<ContentType> = settings.accept,
    ): HttpResponsePort =
        send(PUT, path, headers, body, formParameters, parts, contentType, accept)

    fun put(
        path: String = "/",
        formParameters: List<FormParameter> = emptyList(),
        headers: Map<String, *> = emptyMap<String, Any>(),
        parts: List<HttpPart> = emptyList(),
        contentType: ContentType? = settings.contentType,
        accept: List<ContentType> = settings.accept,
        body: () -> Any,
    ): HttpResponsePort =
        send(PUT, path, headers, body(), formParameters, parts, contentType, accept)

    fun post(
        path: String = "/",
        headers: Map<String, *> = emptyMap<String, Any>(),
        body: Any = "",
        formParameters: List<FormParameter> = emptyList(),
        parts: List<HttpPart> = emptyList(),
        contentType: ContentType? = settings.contentType,
        accept: List<ContentType> = settings.accept,
    ): HttpResponsePort =
        send(POST, path, headers, body, formParameters, parts, contentType, accept)

    fun options(
        path: String = "/",
        headers: Map<String, *> = emptyMap<String, Any>(),
        body: Any = "",
        formParameters: List<FormParameter> = emptyList(),
        parts: List<HttpPart> = emptyList(),
        contentType: ContentType? = settings.contentType,
        accept: List<ContentType> = settings.accept,
    ): HttpResponsePort =
        send(OPTIONS, path, headers, body, formParameters, parts, contentType, accept)

    fun delete(
        path: String = "/",
        headers: Map<String, *> = emptyMap<String, Any>(),
        body: Any = "",
        formParameters: List<FormParameter> = emptyList(),
        parts: List<HttpPart> = emptyList(),
        contentType: ContentType? = settings.contentType,
        accept: List<ContentType> = settings.accept,
    ): HttpResponsePort =
        send(DELETE, path, headers, body, formParameters, parts, contentType, accept)

    fun patch(
        path: String = "/",
        headers: Map<String, *> = emptyMap<String, Any>(),
        body: Any = "",
        formParameters: List<FormParameter> = emptyList(),
        parts: List<HttpPart> = emptyList(),
        contentType: ContentType? = settings.contentType,
        accept: List<ContentType> = settings.accept,
    ): HttpResponsePort =
        send(PATCH, path, headers, body, formParameters, parts, contentType, accept)

    fun trace(
        path: String = "/",
        headers: Map<String, *> = emptyMap<String, Any>(),
        body: Any = "",
        formParameters: List<FormParameter> = emptyList(),
        parts: List<HttpPart> = emptyList(),
        contentType: ContentType? = settings.contentType,
        accept: List<ContentType> = settings.accept,
    ): HttpResponsePort =
        send(TRACE, path, headers, body, formParameters, parts, contentType, accept)

    fun get(
        pathPattern: String,
        pathParameters: Map<String, Any>,
        headers: Map<String, *> = emptyMap<String, Any>(),
        body: Any = "",
        formParameters: List<FormParameter> = emptyList(),
        parts: List<HttpPart> = emptyList(),
        contentType: ContentType? = settings.contentType,
        accept: List<ContentType> = settings.accept,
    ): HttpResponsePort =
        send(
            GET, pathPattern, pathParameters, headers, body, formParameters, parts, contentType, accept
        )

    fun put(
        pathPattern: String,
        pathParameters: Map<String, Any>,
        headers: Map<String, *> = emptyMap<String, Any>(),
        body: Any = "",
        formParameters: List<FormParameter> = emptyList(),
        parts: List<HttpPart> = emptyList(),
        contentType: ContentType? = settings.contentType,
        accept: List<ContentType> = settings.accept,
    ): HttpResponsePort =
        send(
            PUT, pathPattern, pathParameters, headers, body, formParameters, parts, contentType, accept
        )

    fun put(
        pathPattern: String,
        pathParameters: Map<String, Any>,
        formParameters: List<FormParameter> = emptyList(),
        parts: List<HttpPart> = emptyList(),
        contentType: ContentType? = settings.contentType,
        accept: List<ContentType> = settings.accept,
        body: () -> Any,
    ): HttpResponsePort =
        send(
            PUT, pathPattern, pathParameters, Headers(), body(), formParameters, parts, contentType, accept
        )

    fun post(
        pathPattern: String,
        pathParameters: Map<String, Any>,
        headers: Map<String, *> = emptyMap<String, Any>(),
        body: Any = "",
        formParameters: List<FormParameter> = emptyList(),
        parts: List<HttpPart> = emptyList(),
        contentType: ContentType? = settings.contentType,
        accept: List<ContentType> = settings.accept,
    ): HttpResponsePort =
        send(
            POST, pathPattern, pathParameters, headers, body, formParameters, parts, contentType, accept
        )

    fun options(
        pathPattern: String,
        pathParameters: Map<String, Any>,
        headers: Map<String, *> = emptyMap<String, Any>(),
        body: Any = "",
        formParameters: List<FormParameter> = emptyList(),
        parts: List<HttpPart> = emptyList(),
        contentType: ContentType? = settings.contentType,
        accept: List<ContentType> = settings.accept,
    ): HttpResponsePort =
        send(
            OPTIONS, pathPattern, pathParameters, headers, body, formParameters, parts, contentType, accept
        )

    fun delete(
        pathPattern: String,
        pathParameters: Map<String, Any>,
        headers: Map<String, *> = emptyMap<String, Any>(),
        body: Any = "",
        formParameters: List<FormParameter> = emptyList(),
        parts: List<HttpPart> = emptyList(),
        contentType: ContentType? = settings.contentType,
        accept: List<ContentType> = settings.accept,
    ): HttpResponsePort =
        send(
            DELETE, pathPattern, pathParameters, headers, body, formParameters, parts, contentType, accept
        )

    fun patch(
        pathPattern: String,
        pathParameters: Map<String, Any>,
        headers: Map<String, *> = emptyMap<String, Any>(),
        body: Any = "",
        formParameters: List<FormParameter> = emptyList(),
        parts: List<HttpPart> = emptyList(),
        contentType: ContentType? = settings.contentType,
        accept: List<ContentType> = settings.accept,
    ): HttpResponsePort =
        send(
            PATCH, pathPattern, pathParameters, headers, body, formParameters, parts, contentType, accept
        )

    fun trace(
        pathPattern: String,
        pathParameters: Map<String, Any>,
        headers: Map<String, *> = emptyMap<String, Any>(),
        body: Any = "",
        formParameters: List<FormParameter> = emptyList(),
        parts: List<HttpPart> = emptyList(),
        contentType: ContentType? = settings.contentType,
        accept: List<ContentType> = settings.accept,
    ): HttpResponsePort =
        send(
            TRACE, pathPattern, pathParameters, headers, body, formParameters, parts, contentType, accept
        )
}
