package com.hexagontk.rest.tools

import com.hexagontk.core.media.MediaType
import com.hexagontk.http.SslSettings
import com.hexagontk.http.client.HttpClient
import com.hexagontk.http.client.HttpClientPort
import com.hexagontk.http.client.HttpClientSettings
import com.hexagontk.http.handlers.BeforeHandler
import com.hexagontk.http.handlers.HttpHandler
import com.hexagontk.http.model.HttpRequest
import com.hexagontk.http.model.*
import com.hexagontk.http.model.HttpMethod.*
import com.hexagontk.http.patterns.createPathPattern
import com.hexagontk.rest.SerializeRequestCallback
import java.net.URI

class HttpClientTool(
    adapter: HttpClientPort,
    url: String? = null,
    httpContentType: ContentType? = null,
    httpAccept: List<ContentType> = emptyList(),
    val httpHeaders: Map<String, *> = emptyMap<String, Any>(),
    sslSettings: SslSettings? = SslSettings(),
    val handler: HttpHandler? = serializeHandler,
    authorization: Authorization? = null,
    followRedirects: Boolean = false
) {
    companion object {
        // TODO Use SerializeRequestHandler when created
        val serializeHandler: HttpHandler = BeforeHandler("*", SerializeRequestCallback())
    }

    private val settings =
        HttpClientSettings(
            baseUri = url?.let(::URI),
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
    private lateinit var lastResponse: HttpResponsePort

    val request: HttpRequest get() = lastRequest
    val response: HttpResponsePort get() = lastResponse
    val status: Int get() = lastResponse.status
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

    fun assertStatus(status: Int) {
        assert(status == lastResponse.status)
    }

    fun assertOk() {
        assertStatus(OK_200)
    }

    fun assertSuccess() {
        assert(lastResponse.status in SUCCESS)
    }

    fun assertContentType(contentType: ContentType) {
        assert(this.contentType?.text == contentType.text)
    }

    fun assertContentType(mediaType: MediaType) {
        assertContentType(ContentType(mediaType))
    }

    fun assertBody(body: Any) {
        assert(body == lastResponse.body)
    }

    fun assertBodyContains(vararg texts: String) {
        texts.forEach { assert(lastResponse.bodyString().contains(it)) }
    }

    private fun toHeaders(map: Map<String, *>): Headers = Headers(
        map.flatMap { (k, v) ->
            when (v) {
                is Collection<*> -> v.map { Header(k, it) }.toList()
                else -> listOf(Header(k, v))
            }
        }
    )

    private fun send(
        method: HttpMethod,
        path: String?,
        body: Any = "",
        formParameters: List<HttpField> = emptyList(),
        headers: Map<String, *> = emptyMap<String, Any>(),
        parts: List<HttpPart> = emptyList(),
        contentType: ContentType? = settings.contentType,
        accept: List<ContentType> = settings.accept,
        pathPattern: String? = null,
        pathParameters: Map<String, Any> = emptyMap(),
    ): HttpResponsePort =
        client
            .apply {
                if (!started())
                    start()

                val (pattern, patternPath) = when {
                    pathPattern != null ->
                        createPathPattern(pathPattern)
                            .let { it to it.insertParameters(pathParameters) }

                    else -> null to (path ?: "")
                }

                lastRequest = HttpRequest(
                    method = method,
                    path = patternPath,
                    body = body,
                    headers = toHeaders(headers),
                    formParameters = Parameters(formParameters),
                    parts = parts,
                    contentType = contentType,
                    accept = accept,
                    pathPattern = pattern,
                    pathParameters = pathParameters,
                )
            }
            .send(lastRequest)
            .apply { lastResponse = this }

    private fun send(
        method: HttpMethod,
        pathPattern: String,
        pathParameters: Map<String, Any>,
        body: Any = "",
        formParameters: List<HttpField> = emptyList(),
        headers: Map<String, *> = emptyMap<String, Any>(),
        parts: List<HttpPart> = emptyList(),
        contentType: ContentType? = settings.contentType,
        accept: List<ContentType> = settings.accept,
    ): HttpResponsePort =
        send(
            method,
            null,
            body,
            formParameters,
            headers,
            parts,
            contentType,
            accept,
            pathPattern,
            pathParameters,
        )

    fun get(
        path: String = "/",
        body: Any = "",
        formParameters: List<HttpField> = emptyList(),
        headers: Map<String, *> = emptyMap<String, Any>(),
        parts: List<HttpPart> = emptyList(),
        contentType: ContentType? = settings.contentType,
        accept: List<ContentType> = settings.accept,
    ): HttpResponsePort =
        send(GET, path, body, formParameters, headers, parts, contentType, accept)

    fun put(
        path: String = "/",
        body: Any = "",
        formParameters: List<HttpField> = emptyList(),
        headers: Map<String, *> = emptyMap<String, Any>(),
        parts: List<HttpPart> = emptyList(),
        contentType: ContentType? = settings.contentType,
        accept: List<ContentType> = settings.accept,
    ): HttpResponsePort =
        send(PUT, path, body, formParameters, headers, parts, contentType, accept)

    fun put(
        path: String = "/",
        formParameters: List<HttpField> = emptyList(),
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
        formParameters: List<HttpField> = emptyList(),
        headers: Map<String, *> = emptyMap<String, Any>(),
        parts: List<HttpPart> = emptyList(),
        contentType: ContentType? = settings.contentType,
        accept: List<ContentType> = settings.accept,
    ): HttpResponsePort =
        send(POST, path, body, formParameters, headers, parts, contentType, accept)

    fun post(
        path: String = "/",
        formParameters: List<HttpField> = emptyList(),
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
        formParameters: List<HttpField> = emptyList(),
        headers: Map<String, *> = emptyMap<String, Any>(),
        parts: List<HttpPart> = emptyList(),
        contentType: ContentType? = settings.contentType,
        accept: List<ContentType> = settings.accept,
    ): HttpResponsePort =
        send(OPTIONS, path, body, formParameters, headers, parts, contentType, accept)

    fun delete(
        path: String = "/",
        body: Any = "",
        formParameters: List<HttpField> = emptyList(),
        headers: Map<String, *> = emptyMap<String, Any>(),
        parts: List<HttpPart> = emptyList(),
        contentType: ContentType? = settings.contentType,
        accept: List<ContentType> = settings.accept,
    ): HttpResponsePort =
        send(DELETE, path, body, formParameters, headers, parts, contentType, accept)

    fun patch(
        path: String = "/",
        body: Any = "",
        formParameters: List<HttpField> = emptyList(),
        headers: Map<String, *> = emptyMap<String, Any>(),
        parts: List<HttpPart> = emptyList(),
        contentType: ContentType? = settings.contentType,
        accept: List<ContentType> = settings.accept,
    ): HttpResponsePort =
        send(PATCH, path, body, formParameters, headers, parts, contentType, accept)

    fun trace(
        path: String = "/",
        body: Any = "",
        formParameters: List<HttpField> = emptyList(),
        headers: Map<String, *> = emptyMap<String, Any>(),
        parts: List<HttpPart> = emptyList(),
        contentType: ContentType? = settings.contentType,
        accept: List<ContentType> = settings.accept,
    ): HttpResponsePort =
        send(TRACE, path, body, formParameters, headers, parts, contentType, accept)

    fun get(
        path: String,
        pathParameters: Map<String, Any>,
        body: Any = "",
        formParameters: List<HttpField> = emptyList(),
        headers: Map<String, *> = emptyMap<String, Any>(),
        parts: List<HttpPart> = emptyList(),
        contentType: ContentType? = settings.contentType,
        accept: List<ContentType> = settings.accept,
    ): HttpResponsePort =
        send(GET, path, pathParameters, body, formParameters, headers, parts, contentType, accept)

    fun put(
        path: String,
        pathParameters: Map<String, Any>,
        body: Any = "",
        formParameters: List<HttpField> = emptyList(),
        headers: Map<String, *> = emptyMap<String, Any>(),
        parts: List<HttpPart> = emptyList(),
        contentType: ContentType? = settings.contentType,
        accept: List<ContentType> = settings.accept,
    ): HttpResponsePort =
        send(PUT, path, pathParameters, body, formParameters, headers, parts, contentType, accept)

    fun put(
        path: String,
        pathParameters: Map<String, Any>,
        formParameters: List<HttpField> = emptyList(),
        parts: List<HttpPart> = emptyList(),
        contentType: ContentType? = settings.contentType,
        accept: List<ContentType> = settings.accept,
        body: () -> Any,
    ): HttpResponsePort =
        put(
            path,
            pathParameters,
            body(),
            formParameters,
            mapOf<String, Any>(),
            parts,
            contentType,
            accept
        )

    fun post(
        path: String,
        pathParameters: Map<String, Any>,
        body: Any = "",
        formParameters: List<HttpField> = emptyList(),
        headers: Map<String, *> = emptyMap<String, Any>(),
        parts: List<HttpPart> = emptyList(),
        contentType: ContentType? = settings.contentType,
        accept: List<ContentType> = settings.accept,
    ): HttpResponsePort =
        send(POST, path, pathParameters, body, formParameters, headers, parts, contentType, accept)

    fun post(
        path: String,
        pathParameters: Map<String, Any>,
        formParameters: List<HttpField> = emptyList(),
        headers: Map<String, *> = emptyMap<String, Any>(),
        parts: List<HttpPart> = emptyList(),
        contentType: ContentType? = settings.contentType,
        accept: List<ContentType> = settings.accept,
        body: () -> Any,
    ): HttpResponsePort =
        post(path, pathParameters, body(), formParameters, headers, parts, contentType, accept)

    fun options(
        path: String,
        pathParameters: Map<String, Any>,
        body: Any = "",
        formParameters: List<HttpField> = emptyList(),
        headers: Map<String, *> = emptyMap<String, Any>(),
        parts: List<HttpPart> = emptyList(),
        contentType: ContentType? = settings.contentType,
        accept: List<ContentType> = settings.accept,
    ): HttpResponsePort =
        send(
            OPTIONS, path, pathParameters, body, formParameters, headers, parts, contentType, accept
        )

    fun delete(
        path: String,
        pathParameters: Map<String, Any>,
        body: Any = "",
        formParameters: List<HttpField> = emptyList(),
        headers: Map<String, *> = emptyMap<String, Any>(),
        parts: List<HttpPart> = emptyList(),
        contentType: ContentType? = settings.contentType,
        accept: List<ContentType> = settings.accept,
    ): HttpResponsePort =
        send(
            DELETE, path, pathParameters, body, formParameters, headers, parts, contentType, accept
        )

    fun patch(
        path: String,
        pathParameters: Map<String, Any>,
        body: Any = "",
        formParameters: List<HttpField> = emptyList(),
        headers: Map<String, *> = emptyMap<String, Any>(),
        parts: List<HttpPart> = emptyList(),
        contentType: ContentType? = settings.contentType,
        accept: List<ContentType> = settings.accept,
    ): HttpResponsePort =
        send(PATCH, path, pathParameters, body, formParameters, headers, parts, contentType, accept)

    fun trace(
        path: String,
        pathParameters: Map<String, Any>,
        body: Any = "",
        formParameters: List<HttpField> = emptyList(),
        headers: Map<String, *> = emptyMap<String, Any>(),
        parts: List<HttpPart> = emptyList(),
        contentType: ContentType? = settings.contentType,
        accept: List<ContentType> = settings.accept,
    ): HttpResponsePort =
        send(TRACE, path, pathParameters, body, formParameters, headers, parts, contentType, accept)
}
