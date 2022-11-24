package com.hexagonkt.http.test

import com.hexagonkt.core.media.ApplicationMedia.JSON
import com.hexagonkt.http.SslSettings
import com.hexagonkt.http.client.HttpClient
import com.hexagonkt.http.client.HttpClientPort
import com.hexagonkt.http.client.HttpClientSettings
import com.hexagonkt.http.client.model.HttpClientRequest
import com.hexagonkt.http.client.model.HttpClientResponse
import com.hexagonkt.http.model.*
import com.hexagonkt.http.model.HttpMethod.*
import java.net.URL

data class Http(
    val url: String,
    val adapter: HttpClientPort,
    val contentType: ContentType? = ContentType(JSON),
    val headers: Map<String, *> = emptyMap<String, Any>(),
    val sslSettings: SslSettings? = SslSettings(),
    val block: (Http.() -> Unit)? = null,
) {

    private val settings =
        HttpClientSettings(
            contentType = contentType,
            useCookies = true,
            headers = toHeaders(headers),
            insecure = true,
            sslSettings = sslSettings,
        )

    private val http = HttpClient(adapter, URL(url), settings)//.apply { start() }

    lateinit var response: HttpClientResponse

    init {
        if (block != null)
            http.use { block.invoke(this@Http) }
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
    ): HttpClientResponse =
        http
            .apply {
                if (!started())
                    start()
            }
            .send(
                HttpClientRequest(
                    method = method,
                    path = path,
                    body = body,
                    headers = toHeaders(headers),
                    formParameters = FormParameters(formParameters),
                    parts = parts,
                    contentType = contentType,
                )
            )
            .apply { response = this }

    fun get(
        path: String = "/",
        headers: Map<String, *> = emptyMap<String, Any>(),
        body: Any = "",
        formParameters: List<FormParameter> = emptyList(),
        parts: List<HttpPart> = emptyList(),
        contentType: ContentType? = this.contentType,
    ): HttpClientResponse =
        send(GET, path, headers, body, formParameters, parts, contentType)

    // TODO Test these
    fun put(
        path: String = "/",
        headers: Map<String, *> = emptyMap<String, Any>(),
        body: Any = "",
        formParameters: List<FormParameter> = emptyList(),
        parts: List<HttpPart> = emptyList(),
        contentType: ContentType? = this.contentType,
    ): HttpClientResponse =
        send(PUT, path, headers, body, formParameters, parts, contentType)

    fun post(
        path: String = "/",
        headers: Map<String, *> = emptyMap<String, Any>(),
        body: Any = "",
        formParameters: List<FormParameter> = emptyList(),
        parts: List<HttpPart> = emptyList(),
        contentType: ContentType? = this.contentType,
    ): HttpClientResponse =
        send(POST, path, headers, body, formParameters, parts, contentType)

    fun options(
        path: String = "/",
        headers: Map<String, *> = emptyMap<String, Any>(),
        body: Any = "",
        formParameters: List<FormParameter> = emptyList(),
        parts: List<HttpPart> = emptyList(),
        contentType: ContentType? = this.contentType,
    ): HttpClientResponse =
        send(OPTIONS, path, headers, body, formParameters, parts, contentType)

    fun delete(
        path: String = "/",
        headers: Map<String, *> = emptyMap<String, Any>(),
        body: Any = "",
        formParameters: List<FormParameter> = emptyList(),
        parts: List<HttpPart> = emptyList(),
        contentType: ContentType? = this.contentType,
    ): HttpClientResponse =
        send(DELETE, path, headers, body, formParameters, parts, contentType)

    fun patch(
        path: String = "/",
        headers: Map<String, *> = emptyMap<String, Any>(),
        body: Any = "",
        formParameters: List<FormParameter> = emptyList(),
        parts: List<HttpPart> = emptyList(),
        contentType: ContentType? = this.contentType,
    ): HttpClientResponse =
        send(PATCH, path, headers, body, formParameters, parts, contentType)

    fun trace(
        path: String = "/",
        headers: Map<String, *> = emptyMap<String, Any>(),
        body: Any = "",
        formParameters: List<FormParameter> = emptyList(),
        parts: List<HttpPart> = emptyList(),
        contentType: ContentType? = this.contentType,
    ): HttpClientResponse =
        send(TRACE, path, headers, body, formParameters, parts, contentType)
}
