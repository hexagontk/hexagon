package com.hexagonkt.http.test

import com.hexagonkt.core.MultiMap
import com.hexagonkt.core.fail
import com.hexagonkt.core.media.ApplicationMedia.JSON
import com.hexagonkt.core.media.TextMedia
import com.hexagonkt.core.require
import com.hexagonkt.serialization.SerializationManager
import com.hexagonkt.http.SslSettings
import com.hexagonkt.http.client.HttpClient
import com.hexagonkt.http.client.HttpClientSettings
import com.hexagonkt.http.client.jetty.JettyClientAdapter
import com.hexagonkt.http.client.model.HttpClientRequest
import com.hexagonkt.http.client.model.HttpClientResponse
import com.hexagonkt.http.model.ClientErrorStatus.NOT_FOUND
import com.hexagonkt.http.model.ContentType
import com.hexagonkt.http.model.HttpMethod
import com.hexagonkt.http.model.HttpMethod.*
import com.hexagonkt.http.model.HttpPart
import com.hexagonkt.http.model.SuccessStatus.OK
import com.hexagonkt.http.server.HttpServer
import com.hexagonkt.http.server.callbacks.FileCallback
import com.hexagonkt.http.server.handlers.PathHandler
import com.hexagonkt.http.server.handlers.path
import com.hexagonkt.http.server.jetty.serve
import com.hexagonkt.serialization.jackson.json.Json
import com.hexagonkt.serialization.parse
import com.hexagonkt.serialization.serialize
import org.junit.jupiter.api.AfterAll
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestInstance
import org.junit.jupiter.api.TestInstance.Lifecycle.PER_CLASS
import java.io.File
import java.net.URL
import java.nio.file.Path
import kotlin.test.assertEquals

@TestInstance(PER_CLASS)
class ApiTest {
    private var dynamicPath: PathHandler = PathHandler()

    private val server: HttpServer by lazy {
        val servedPath: Path = File(System.getProperty("user.dir")).toPath()

        SerializationManager.formats = linkedSetOf(Json)

        serve {
            after(pattern = "*", status = NOT_FOUND) {
                // Dynamic resolution picking from dynamicPath: PathHandler
                copy(
                    context = context.copy(
                        event = context.event.copy(
                            response = dynamicPath.process(request)
                        )
                    )
                )
            }

            get("/dir/*", FileCallback(servedPath.toFile()))

            get("/hello/{name}") {
                val name = pathParameters["name"]

                ok("Hello, $name!", contentType = ContentType(TextMedia.PLAIN))
            }

            put("/data/{id}") {
                val id = pathParameters.require("id")
                val data = request.bodyString().parse()
                val content = mapOf(id to data)

                ok(content.serialize(Json), contentType = ContentType(JSON))
            }
        }
    }

    @AfterAll fun `Set up mock services`() {
        server.stop()
    }

    @Test fun `Do HTTP requests`() {
        val http = Http("http://localhost:${server.runtimePort}")
        http.get("/hello/mike")
        assertEquals(OK, http.responseOrNull?.status)
    }

    @Test fun `Mock HTTP response`() {
        dynamicPath = path {
            get("/foo") {
                ok("dynamic")
            }
        }

        val http = Http("http://localhost:${server.runtimePort}")
        http.get("/foo")
        assertEquals(OK, http.responseOrNull?.status)
        assertEquals("dynamic", http.responseOrNull?.body)
        dynamicPath = path {
            get("/foo") {
                ok("changed")
            }
        }
        http.get("/foo")
        assertEquals(OK, http.responseOrNull?.status)
        assertEquals(OK, http.response.status)
        assertEquals("changed", http.responseOrNull?.body)
        assertEquals("changed", http.response.body)
    }

    data class Http(
        val url: String,
        val contentType: ContentType? = ContentType(JSON),
        val headers: Map<String, *> = emptyMap<String, Any>(),
        val sslSettings: SslSettings? = SslSettings(),
    ) {

        private val settings =
            HttpClientSettings(
                contentType = contentType,
                useCookies = true,
                headers = toMultiMap(headers),
                insecure = true,
                sslSettings = sslSettings,
            )

        private val http = HttpClient(JettyClientAdapter(), URL(url), settings).apply { start() }

        var responseOrNull: HttpClientResponse? = null
        val response: HttpClientResponse
            get() = responseOrNull ?: fail

        private fun toMultiMap(map: Map<String, *>): MultiMap<String, String> = MultiMap(
            map.mapValues { (_, v) ->
                when (v) {
                    is List<*> -> v.map { it.toString() }
                    else -> listOf(v.toString())
                }
            }
        )

        private fun send(
            method: HttpMethod = GET,
            path: String = "/",
            headers: Map<String, *> = emptyMap<String, Any>(),
            body: Any = "",
            formParameters: Map<String, *> = emptyMap<String, Any>(),
            parts: List<HttpPart> = emptyList(),
            contentType: ContentType? = this.contentType,
        ): HttpClientResponse =
            http.send(HttpClientRequest(
                method = method,
                path = path,
                body = body,
                headers = toMultiMap(headers),
                formParameters = toMultiMap(formParameters),
                parts = parts,
                contentType = contentType,
            )
            ).apply { responseOrNull = this }

        fun get(
            path: String = "/",
            headers: Map<String, *> = emptyMap<String, Any>(),
            body: Any = "",
            formParameters: Map<String, *> = emptyMap<String, Any>(),
            parts: List<HttpPart> = emptyList(),
            contentType: ContentType? = this.contentType,
        ): HttpClientResponse =
            send(GET, path, headers, body, formParameters, parts, contentType)

        fun put(
            path: String = "/",
            headers: Map<String, *> = emptyMap<String, Any>(),
            body: Any = "",
            formParameters: Map<String, *> = emptyMap<String, Any>(),
            parts: List<HttpPart> = emptyList(),
            contentType: ContentType? = this.contentType,
        ): HttpClientResponse =
            send(PUT, path, headers, body, formParameters, parts, contentType)

        fun post(
            path: String = "/",
            headers: Map<String, *> = emptyMap<String, Any>(),
            body: Any = "",
            formParameters: Map<String, *> = emptyMap<String, Any>(),
            parts: List<HttpPart> = emptyList(),
            contentType: ContentType? = this.contentType,
        ): HttpClientResponse =
            send(POST, path, headers, body, formParameters, parts, contentType)

        fun options(
            path: String = "/",
            headers: Map<String, *> = emptyMap<String, Any>(),
            body: Any = "",
            formParameters: Map<String, *> = emptyMap<String, Any>(),
            parts: List<HttpPart> = emptyList(),
            contentType: ContentType? = this.contentType,
        ): HttpClientResponse =
            send(OPTIONS, path, headers, body, formParameters, parts, contentType)

        fun delete(
            path: String = "/",
            headers: Map<String, *> = emptyMap<String, Any>(),
            body: Any = "",
            formParameters: Map<String, *> = emptyMap<String, Any>(),
            parts: List<HttpPart> = emptyList(),
            contentType: ContentType? = this.contentType,
        ): HttpClientResponse =
            send(DELETE, path, headers, body, formParameters, parts, contentType)

        fun patch(
            path: String = "/",
            headers: Map<String, *> = emptyMap<String, Any>(),
            body: Any = "",
            formParameters: Map<String, *> = emptyMap<String, Any>(),
            parts: List<HttpPart> = emptyList(),
            contentType: ContentType? = this.contentType,
        ): HttpClientResponse =
            send(PATCH, path, headers, body, formParameters, parts, contentType)

        fun trace(
            path: String = "/",
            headers: Map<String, *> = emptyMap<String, Any>(),
            body: Any = "",
            formParameters: Map<String, *> = emptyMap<String, Any>(),
            parts: List<HttpPart> = emptyList(),
            contentType: ContentType? = this.contentType,
        ): HttpClientResponse =
            send(TRACE, path, headers, body, formParameters, parts, contentType)

        fun head(
            path: String = "/",
            headers: Map<String, *> = emptyMap<String, Any>(),
            body: Any = "",
            formParameters: Map<String, *> = emptyMap<String, Any>(),
            parts: List<HttpPart> = emptyList(),
            contentType: ContentType? = this.contentType,
        ): HttpClientResponse =
            send(HEAD, path, headers, body, formParameters, parts, contentType)
    }
}
