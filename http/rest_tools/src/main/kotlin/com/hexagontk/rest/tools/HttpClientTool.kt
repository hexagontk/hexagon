package com.hexagontk.rest.tools

import com.hexagontk.core.media.MediaType
import com.hexagontk.core.urlOf
import com.hexagontk.http.SslSettings
import com.hexagontk.http.client.HttpClient
import com.hexagontk.http.client.HttpClientPort
import com.hexagontk.http.client.HttpClientSettings
import com.hexagontk.http.handlers.BeforeHandler
import com.hexagontk.http.handlers.HttpHandler
import com.hexagontk.http.model.HttpRequest
import com.hexagontk.http.model.*
import com.hexagontk.http.model.HttpMethod.*
import com.hexagontk.http.model.HttpStatusType.SUCCESS
import com.hexagontk.http.patterns.createPathPattern
import com.hexagontk.rest.SerializeRequestCallback

data class HttpClientTool(
    val adapter: HttpClientPort,
    val url: String? = null,
    val httpContentType: ContentType? = null,
    val httpAccept: List<ContentType> = emptyList(),
    val httpHeaders: Map<String, *> = emptyMap<String, Any>(),
    val sslSettings: SslSettings? = SslSettings(),
    val handler: HttpHandler? = serializeHandler,
    val authorization: Authorization? = null,
    val followRedirects: Boolean = false
) {
    companion object {
        // TODO Use SerializeRequestHandler when created
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
            authorization = authorization,
            followRedirects = followRedirects
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

    fun request(block: HttpClientTool.() -> Unit) {
        client.request { block.invoke(this@HttpClientTool) }
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

    fun assertContentType(contentType: ContentType) {
        assert(this.contentType == contentType)
    }

    fun assertContentType(mediaType: MediaType) {
        assert(contentType == ContentType(mediaType))
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
        body: Any = "",
        formParameters: List<FormParameter> = emptyList(),
        headers: Map<String, *> = emptyMap<String, Any>(),
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
        path: Pair<String, Map<String, Any>>,
        body: Any = "",
        formParameters: List<FormParameter> = emptyList(),
        headers: Map<String, *> = emptyMap<String, Any>(),
        parts: List<HttpPart> = emptyList(),
        contentType: ContentType? = settings.contentType,
        accept: List<ContentType> = settings.accept,
        attributes: Map<String, Any> = emptyMap(),
    ): HttpResponsePort =
        send(
            method = method,
            path = createPathPattern(path.first, false).insertParameters(path.second),
            body = body,
            formParameters = formParameters,
            headers = toHeaders(headers),
            parts = parts,
            contentType = contentType,
            accept = accept,
            attributes = attributes
                + mapOf("pathPattern" to path.first, "pathParameters" to path.second),
        )

    fun get(
        path: String = "/",
        body: Any = "",
        formParameters: List<FormParameter> = emptyList(),
        headers: Map<String, *> = emptyMap<String, Any>(),
        parts: List<HttpPart> = emptyList(),
        contentType: ContentType? = settings.contentType,
        accept: List<ContentType> = settings.accept,
    ): HttpResponsePort =
        send(GET, path, body, formParameters, headers, parts, contentType, accept)

    fun put(
        path: String = "/",
        body: Any = "",
        formParameters: List<FormParameter> = emptyList(),
        headers: Map<String, *> = emptyMap<String, Any>(),
        parts: List<HttpPart> = emptyList(),
        contentType: ContentType? = settings.contentType,
        accept: List<ContentType> = settings.accept,
    ): HttpResponsePort =
        send(PUT, path, body, formParameters, headers, parts, contentType, accept)

    fun put(
        path: String = "/",
        formParameters: List<FormParameter> = emptyList(),
        headers: Map<String, *> = emptyMap<String, Any>(),
        parts: List<HttpPart> = emptyList(),
        contentType: ContentType? = settings.contentType,
        accept: List<ContentType> = settings.accept,
        body: () -> Any,
    ): HttpResponsePort =
        put(path, body(), formParameters, headers, parts, contentType, accept)

    fun post(
        path: String = "/",
        body: Any = "",
        formParameters: List<FormParameter> = emptyList(),
        headers: Map<String, *> = emptyMap<String, Any>(),
        parts: List<HttpPart> = emptyList(),
        contentType: ContentType? = settings.contentType,
        accept: List<ContentType> = settings.accept,
    ): HttpResponsePort =
        send(POST, path, body, formParameters, headers, parts, contentType, accept)

    fun post(
        path: String = "/",
        formParameters: List<FormParameter> = emptyList(),
        headers: Map<String, *> = emptyMap<String, Any>(),
        parts: List<HttpPart> = emptyList(),
        contentType: ContentType? = settings.contentType,
        accept: List<ContentType> = settings.accept,
        body: () -> Any,
    ): HttpResponsePort =
        post(path, body(), formParameters, headers, parts, contentType, accept)

    fun options(
        path: String = "/",
        body: Any = "",
        formParameters: List<FormParameter> = emptyList(),
        headers: Map<String, *> = emptyMap<String, Any>(),
        parts: List<HttpPart> = emptyList(),
        contentType: ContentType? = settings.contentType,
        accept: List<ContentType> = settings.accept,
    ): HttpResponsePort =
        send(OPTIONS, path, body, formParameters, headers, parts, contentType, accept)

    fun delete(
        path: String = "/",
        body: Any = "",
        formParameters: List<FormParameter> = emptyList(),
        headers: Map<String, *> = emptyMap<String, Any>(),
        parts: List<HttpPart> = emptyList(),
        contentType: ContentType? = settings.contentType,
        accept: List<ContentType> = settings.accept,
    ): HttpResponsePort =
        send(DELETE, path, body, formParameters, headers, parts, contentType, accept)

    fun patch(
        path: String = "/",
        body: Any = "",
        formParameters: List<FormParameter> = emptyList(),
        headers: Map<String, *> = emptyMap<String, Any>(),
        parts: List<HttpPart> = emptyList(),
        contentType: ContentType? = settings.contentType,
        accept: List<ContentType> = settings.accept,
    ): HttpResponsePort =
        send(PATCH, path, body, formParameters, headers, parts, contentType, accept)

    fun trace(
        path: String = "/",
        body: Any = "",
        formParameters: List<FormParameter> = emptyList(),
        headers: Map<String, *> = emptyMap<String, Any>(),
        parts: List<HttpPart> = emptyList(),
        contentType: ContentType? = settings.contentType,
        accept: List<ContentType> = settings.accept,
    ): HttpResponsePort =
        send(TRACE, path, body, formParameters, headers, parts, contentType, accept)

    fun get(
        path: Pair<String, Map<String, Any>>,
        body: Any = "",
        formParameters: List<FormParameter> = emptyList(),
        headers: Map<String, *> = emptyMap<String, Any>(),
        parts: List<HttpPart> = emptyList(),
        contentType: ContentType? = settings.contentType,
        accept: List<ContentType> = settings.accept,
    ): HttpResponsePort =
        send(GET, path, body, formParameters, headers, parts, contentType, accept)

    fun put(
        path: Pair<String, Map<String, Any>>,
        body: Any = "",
        formParameters: List<FormParameter> = emptyList(),
        headers: Map<String, *> = emptyMap<String, Any>(),
        parts: List<HttpPart> = emptyList(),
        contentType: ContentType? = settings.contentType,
        accept: List<ContentType> = settings.accept,
    ): HttpResponsePort =
        send(PUT, path, body, formParameters, headers, parts, contentType, accept)

    fun put(
        path: Pair<String, Map<String, Any>>,
        formParameters: List<FormParameter> = emptyList(),
        parts: List<HttpPart> = emptyList(),
        contentType: ContentType? = settings.contentType,
        accept: List<ContentType> = settings.accept,
        body: () -> Any,
    ): HttpResponsePort =
        put(path, body(), formParameters, Headers(), parts, contentType, accept)

    fun post(
        path: Pair<String, Map<String, Any>>,
        body: Any = "",
        formParameters: List<FormParameter> = emptyList(),
        headers: Map<String, *> = emptyMap<String, Any>(),
        parts: List<HttpPart> = emptyList(),
        contentType: ContentType? = settings.contentType,
        accept: List<ContentType> = settings.accept,
    ): HttpResponsePort =
        send(POST, path, body, formParameters, headers, parts, contentType, accept)

    fun post(
        path: Pair<String, Map<String, Any>>,
        formParameters: List<FormParameter> = emptyList(),
        headers: Map<String, *> = emptyMap<String, Any>(),
        parts: List<HttpPart> = emptyList(),
        contentType: ContentType? = settings.contentType,
        accept: List<ContentType> = settings.accept,
        body: () -> Any,
    ): HttpResponsePort =
        post(path, body(), formParameters, headers, parts, contentType, accept)

    fun options(
        path: Pair<String, Map<String, Any>>,
        body: Any = "",
        formParameters: List<FormParameter> = emptyList(),
        headers: Map<String, *> = emptyMap<String, Any>(),
        parts: List<HttpPart> = emptyList(),
        contentType: ContentType? = settings.contentType,
        accept: List<ContentType> = settings.accept,
    ): HttpResponsePort =
        send(OPTIONS, path, body, formParameters, headers, parts, contentType, accept)

    fun delete(
        path: Pair<String, Map<String, Any>>,
        body: Any = "",
        formParameters: List<FormParameter> = emptyList(),
        headers: Map<String, *> = emptyMap<String, Any>(),
        parts: List<HttpPart> = emptyList(),
        contentType: ContentType? = settings.contentType,
        accept: List<ContentType> = settings.accept,
    ): HttpResponsePort =
        send(DELETE, path, body, formParameters, headers, parts, contentType, accept)

    fun patch(
        path: Pair<String, Map<String, Any>>,
        body: Any = "",
        formParameters: List<FormParameter> = emptyList(),
        headers: Map<String, *> = emptyMap<String, Any>(),
        parts: List<HttpPart> = emptyList(),
        contentType: ContentType? = settings.contentType,
        accept: List<ContentType> = settings.accept,
    ): HttpResponsePort =
        send(PATCH, path, body, formParameters, headers, parts, contentType, accept)

    fun trace(
        path: Pair<String, Map<String, Any>>,
        body: Any = "",
        formParameters: List<FormParameter> = emptyList(),
        headers: Map<String, *> = emptyMap<String, Any>(),
        parts: List<HttpPart> = emptyList(),
        contentType: ContentType? = settings.contentType,
        accept: List<ContentType> = settings.accept,
    ): HttpResponsePort =
        send(TRACE, path, body, formParameters, headers, parts, contentType, accept)
}
