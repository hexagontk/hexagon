package com.hexagonkt.http.test

import com.hexagonkt.core.media.MediaType
import com.hexagonkt.http.SslSettings
import com.hexagonkt.http.client.HttpClient
import com.hexagonkt.http.client.HttpClientPort
import com.hexagonkt.http.client.HttpClientSettings
import com.hexagonkt.http.handlers.HttpHandler
import com.hexagonkt.http.model.HttpRequest
import com.hexagonkt.http.model.*
import com.hexagonkt.http.model.HttpMethod.*
import com.hexagonkt.http.patterns.createPathPattern
import java.net.URL

data class Http(
    val adapter: HttpClientPort,
    val url: String? = null,
    val contentType: ContentType? = null,
    val headers: Map<String, *> = emptyMap<String, Any>(),
    val sslSettings: SslSettings? = SslSettings(),
    val handler: HttpHandler? = null,
) {
    companion object {
        fun http(
            adapter: HttpClientPort,
            url: String? = null,
            contentType: ContentType? = null,
            headers: Map<String, *> = emptyMap<String, Any>(),
            sslSettings: SslSettings? = SslSettings(),
            handler: HttpHandler? = null,
            block: Http.() -> Unit
        ) {
           Http(adapter, url, contentType, headers, sslSettings, handler).request(block)
        }

        fun http(
            adapter: HttpClientPort,
            url: String? = null,
            mediaType: MediaType,
            headers: Map<String, *> = emptyMap<String, Any>(),
            sslSettings: SslSettings? = SslSettings(),
            handler: HttpHandler? = null,
            block: Http.() -> Unit
        ) {
            Http(adapter, url, mediaType, headers, sslSettings, handler).request(block)
        }
    }

    private val settings =
        HttpClientSettings(
            baseUrl = url?.let(::URL),
            contentType = contentType,
            useCookies = true,
            headers = toHeaders(headers),
            insecure = true,
            sslSettings = sslSettings,
        )

    private val client = HttpClient(adapter, settings, handler = handler)
    private lateinit var lastResponse: HttpResponsePort
    val response: HttpResponsePort get() = lastResponse

    constructor(
        adapter: HttpClientPort,
        url: String? = null,
        mediaType: MediaType,
        headers: Map<String, *> = emptyMap<String, Any>(),
        sslSettings: SslSettings? = SslSettings(),
        handler: HttpHandler? = null,
    ) : this(adapter, url, ContentType(mediaType), headers, sslSettings, handler)

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
        contentType: ContentType? = this.contentType,
        attributes: Map<String, Any> = emptyMap(),
    ): HttpResponsePort =
        client
            .apply {
                if (!started())
                    start()
            }
            .send(
                HttpRequest(
                    method = method,
                    path = path,
                    body = body,
                    headers = toHeaders(headers),
                    formParameters = FormParameters(formParameters),
                    parts = parts,
                    contentType = contentType,
                ),
                attributes = attributes,
            )
            .apply { lastResponse = this }

    private fun send(
        method: HttpMethod = GET,
        pathPattern: String,
        pathParameters: Map<String, Any>,
        headers: Map<String, *> = emptyMap<String, Any>(),
        body: Any = "",
        formParameters: List<FormParameter> = emptyList(),
        parts: List<HttpPart> = emptyList(),
        contentType: ContentType? = this.contentType,
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
            attributes = attributes
                + mapOf("pathPattern" to pathPattern, "pathParameters" to pathParameters),
        )

    fun get(
        path: String = "/",
        headers: Map<String, *> = emptyMap<String, Any>(),
        body: Any = "",
        formParameters: List<FormParameter> = emptyList(),
        parts: List<HttpPart> = emptyList(),
        contentType: ContentType? = this.contentType,
    ): HttpResponsePort =
        send(GET, path, headers, body, formParameters, parts, contentType)

    fun put(
        path: String = "/",
        body: Any = "",
        headers: Map<String, *> = emptyMap<String, Any>(),
        formParameters: List<FormParameter> = emptyList(),
        parts: List<HttpPart> = emptyList(),
        contentType: ContentType? = this.contentType,
    ): HttpResponsePort =
        send(PUT, path, headers, body, formParameters, parts, contentType)

    fun put(
        path: String = "/",
        formParameters: List<FormParameter> = emptyList(),
        headers: Map<String, *> = emptyMap<String, Any>(),
        parts: List<HttpPart> = emptyList(),
        contentType: ContentType? = this.contentType,
        body: () -> Any,
    ): HttpResponsePort =
        send(PUT, path, headers, body(), formParameters, parts, contentType)

    fun post(
        path: String = "/",
        headers: Map<String, *> = emptyMap<String, Any>(),
        body: Any = "",
        formParameters: List<FormParameter> = emptyList(),
        parts: List<HttpPart> = emptyList(),
        contentType: ContentType? = this.contentType,
    ): HttpResponsePort =
        send(POST, path, headers, body, formParameters, parts, contentType)

    fun options(
        path: String = "/",
        headers: Map<String, *> = emptyMap<String, Any>(),
        body: Any = "",
        formParameters: List<FormParameter> = emptyList(),
        parts: List<HttpPart> = emptyList(),
        contentType: ContentType? = this.contentType,
    ): HttpResponsePort =
        send(OPTIONS, path, headers, body, formParameters, parts, contentType)

    fun delete(
        path: String = "/",
        headers: Map<String, *> = emptyMap<String, Any>(),
        body: Any = "",
        formParameters: List<FormParameter> = emptyList(),
        parts: List<HttpPart> = emptyList(),
        contentType: ContentType? = this.contentType,
    ): HttpResponsePort =
        send(DELETE, path, headers, body, formParameters, parts, contentType)

    fun patch(
        path: String = "/",
        headers: Map<String, *> = emptyMap<String, Any>(),
        body: Any = "",
        formParameters: List<FormParameter> = emptyList(),
        parts: List<HttpPart> = emptyList(),
        contentType: ContentType? = this.contentType,
    ): HttpResponsePort =
        send(PATCH, path, headers, body, formParameters, parts, contentType)

    fun trace(
        path: String = "/",
        headers: Map<String, *> = emptyMap<String, Any>(),
        body: Any = "",
        formParameters: List<FormParameter> = emptyList(),
        parts: List<HttpPart> = emptyList(),
        contentType: ContentType? = this.contentType,
    ): HttpResponsePort =
        send(TRACE, path, headers, body, formParameters, parts, contentType)

    fun get(
        pathPattern: String,
        pathParameters: Map<String, Any>,
        headers: Map<String, *> = emptyMap<String, Any>(),
        body: Any = "",
        formParameters: List<FormParameter> = emptyList(),
        parts: List<HttpPart> = emptyList(),
        contentType: ContentType? = this.contentType,
    ): HttpResponsePort =
        send(GET, pathPattern, pathParameters, headers, body, formParameters, parts, contentType)

    fun put(
        pathPattern: String,
        pathParameters: Map<String, Any>,
        headers: Map<String, *> = emptyMap<String, Any>(),
        body: Any = "",
        formParameters: List<FormParameter> = emptyList(),
        parts: List<HttpPart> = emptyList(),
        contentType: ContentType? = this.contentType,
    ): HttpResponsePort =
        send(PUT, pathPattern, pathParameters, headers, body, formParameters, parts, contentType)

    fun put(
        pathPattern: String,
        pathParameters: Map<String, Any>,
        formParameters: List<FormParameter> = emptyList(),
        parts: List<HttpPart> = emptyList(),
        contentType: ContentType? = this.contentType,
        body: () -> Any,
    ): HttpResponsePort =
        send(PUT, pathPattern, pathParameters, headers, body(), formParameters, parts, contentType)

    fun post(
        pathPattern: String,
        pathParameters: Map<String, Any>,
        headers: Map<String, *> = emptyMap<String, Any>(),
        body: Any = "",
        formParameters: List<FormParameter> = emptyList(),
        parts: List<HttpPart> = emptyList(),
        contentType: ContentType? = this.contentType,
    ): HttpResponsePort =
        send(POST, pathPattern, pathParameters, headers, body, formParameters, parts, contentType)

    fun options(
        pathPattern: String,
        pathParameters: Map<String, Any>,
        headers: Map<String, *> = emptyMap<String, Any>(),
        body: Any = "",
        formParameters: List<FormParameter> = emptyList(),
        parts: List<HttpPart> = emptyList(),
        contentType: ContentType? = this.contentType,
    ): HttpResponsePort =
        send(OPTIONS, pathPattern, pathParameters, headers, body, formParameters, parts, contentType)

    fun delete(
        pathPattern: String,
        pathParameters: Map<String, Any>,
        headers: Map<String, *> = emptyMap<String, Any>(),
        body: Any = "",
        formParameters: List<FormParameter> = emptyList(),
        parts: List<HttpPart> = emptyList(),
        contentType: ContentType? = this.contentType,
    ): HttpResponsePort =
        send(DELETE, pathPattern, pathParameters, headers, body, formParameters, parts, contentType)

    fun patch(
        pathPattern: String,
        pathParameters: Map<String, Any>,
        headers: Map<String, *> = emptyMap<String, Any>(),
        body: Any = "",
        formParameters: List<FormParameter> = emptyList(),
        parts: List<HttpPart> = emptyList(),
        contentType: ContentType? = this.contentType,
    ): HttpResponsePort =
        send(PATCH, pathPattern, pathParameters, headers, body, formParameters, parts, contentType)

    fun trace(
        pathPattern: String,
        pathParameters: Map<String, Any>,
        headers: Map<String, *> = emptyMap<String, Any>(),
        body: Any = "",
        formParameters: List<FormParameter> = emptyList(),
        parts: List<HttpPart> = emptyList(),
        contentType: ContentType? = this.contentType,
    ): HttpResponsePort =
        send(TRACE, pathPattern, pathParameters, headers, body, formParameters, parts, contentType)
}
