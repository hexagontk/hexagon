package com.hexagontk.http.test.examples

import com.hexagontk.core.require
import com.hexagontk.core.media.APPLICATION_JSON
import com.hexagontk.core.media.APPLICATION_YAML
import com.hexagontk.core.urlOf
import com.hexagontk.http.SslSettings
import com.hexagontk.http.client.HttpClient
import com.hexagontk.http.client.HttpClientPort
import com.hexagontk.http.client.HttpClientSettings
import com.hexagontk.http.model.HttpRequest
import com.hexagontk.http.model.HttpResponsePort
import com.hexagontk.http.formatQueryString
import com.hexagontk.http.model.*
import com.hexagontk.http.model.HttpMethod.GET
import com.hexagontk.http.model.INTERNAL_SERVER_ERROR_500
import com.hexagontk.http.model.OK_200
import com.hexagontk.http.server.*
import com.hexagontk.http.handlers.HttpCallbackType
import com.hexagontk.http.handlers.HttpHandler
import com.hexagontk.http.handlers.path
import com.hexagontk.http.test.BaseTest
import com.hexagontk.serialization.SerializationFormat
import com.hexagontk.serialization.SerializationManager
import com.hexagontk.serialization.serialize
import org.junit.jupiter.api.*

import java.math.BigInteger
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

// TODO Refactor duplicated code
@Suppress("FunctionName") // This class's functions are intended to be used only in tests
abstract class ClientTest(
    final override val clientAdapter: () -> HttpClientPort,
    final override val serverAdapter: () -> HttpServerPort,
    private val serializationFormats: List<SerializationFormat>,
    final override val serverSettings: HttpServerSettings = HttpServerSettings(),
) : BaseTest() {

    private var callback: HttpCallbackType = { this }

    override val handler: HttpHandler = path {
        post("*") { callback() }
        get("*") { callback() }
        head("*") { callback() }
        put("*") { callback() }
        delete("*") { callback() }
        trace("*") { callback() }
        options("*") { callback() }
        patch("*") { callback() }
    }

    @BeforeAll fun setUpSerializationFormats() {
        SerializationManager.formats = serializationFormats.toSet()
    }

    @BeforeEach fun resetHandler() {
        callback = {
            val contentType = ContentType(APPLICATION_JSON, charset = Charsets.UTF_8)
            val bodyString = request.bodyString()
            val bodyHeader =
                if (bodyString.endsWith("\n") || bodyString.contains("{")) "json"
                else bodyString

            ok(
                body = bodyString,
                headers = response.headers
                    + Header("body", bodyHeader)
                    + Header("ct", request.contentType?.text ?: "")
                    + Header("query-parameters", formatQueryString(queryParameters)),
                contentType = contentType,
            )
        }
    }

    @Test fun `Exceptions are returned as internal server errors`() {
        callback = { error("failure") }

        val response = client.send(HttpRequest())

        assertEquals(INTERNAL_SERVER_ERROR_500, response.status)
        assertTrue(response.bodyString().contains("failure"))
    }

    @Test fun `Redirects are handled correctly correctly`() {
        callback = {
            if (queryParameters["ok"] != null) ok("redirected")
            else found("/foo?ok")
        }

        val response = client.get()
        assertEquals(FOUND_302, response.status)
        assertEquals("/foo?ok", response.headers["location"]?.value)

        val settings = HttpClientSettings(server.binding, followRedirects = true)
        val redirectClient = HttpClient(clientAdapter(), settings).apply { start() }

        val redirectedResponse = redirectClient.get()
        assertEquals(OK_200, redirectedResponse.status)
        assertEquals("redirected", redirectedResponse.bodyString())
    }

    @Test fun `Create HTTP clients`() {
        val adapter = clientAdapter()

        // clientCreation
        HttpClient(adapter)
        HttpClient(adapter, HttpClientSettings(urlOf("http://host:1234/base")))
        // clientCreation

        // clientSettingsCreation
        // All client settings parameters are optionals and provide default values
        HttpClient(adapter, HttpClientSettings(
            baseUrl = urlOf("http://host:1234/base"),
            contentType = ContentType(APPLICATION_JSON),
            useCookies = true,
            headers = Headers(Header("x-api-Key", "cafebabe")), // Headers used in all requests
            insecure = false,               // If true, the client doesn't check server certificates
            sslSettings = SslSettings()     // Key stores settings (check TLS section for details)
        ))
        // clientSettingsCreation
    }

    @Test fun `JSON requests works as expected`() {
        val expectedBody = "{  \"foo\" : \"fighters\",  \"es\" : \"áéíóúÁÉÍÓÚñÑ\"}"
        val map = mapOf("foo" to "fighters", "es" to "áéíóúÁÉÍÓÚñÑ")
        val requestBody = map.serialize(APPLICATION_JSON)

        val response = client.post("/", requestBody, contentType = ContentType(APPLICATION_JSON))
        assertEquals(expectedBody, response.body.toString().trim().replace("[\r\n]".toRegex(), ""))
        assertEquals(ContentType(APPLICATION_JSON).text, response.headers["ct"]?.value)

        val body2 = client.post("/", requestBody).body
        assertEquals(expectedBody, body2.toString().trim().replace("[\r\n]".toRegex(), ""))
    }

    @Test fun `HTTP generic requests work ok`() {

        val defaultFormat = serializationFormats.first()

        // genericRequest
        val request = HttpRequest(
            method = GET,
            path = "/",
            body = mapOf("body" to "payload").serialize(defaultFormat),
            headers = Headers(Header("x-header", "value")),
            queryParameters = QueryParameters(QueryParameter("qp", "qpValue")),
            contentType = ContentType(APPLICATION_JSON)
        )

        val response = client.send(request)
        // genericRequest

        val getResponse = client.get("/queryParameters?qp=qpValue")
        assertEquals("qp=qpValue", getResponse.headers["query-parameters"]?.value)
        assertEquals("qp=qpValue", response.headers["query-parameters"]?.value)
        checkResponse(response, mapOf("body" to "payload"))

        val base = client.settings.baseUrl?.toString()
        HttpClient(clientAdapter(), client.settings.copy(baseUrl = null)).request {
            val secondResponse = get("$base/queryParameters?qp=qpValue")
            assertEquals("qp=qpValue", secondResponse.headers["query-parameters"]?.value)
        }
    }

    @Test fun `HTTP methods without body work ok`() {

        // withoutBodyRequests
        val responseGet = client.get("/")
        val responseHead = client.head("/")
        val responsePost = client.post("/")
        val responsePut = client.put("/")
        val responseDelete = client.delete("/")
        val responseTrace = client.trace("/")
        val responseOptions = client.options("/")
        val responsePatch = client.patch("/")
        // withoutBodyRequests

        checkResponse(responseGet, null)
        checkResponse(responseHead, null)
        checkResponse(responsePost, null)
        checkResponse(responsePut, null)
        checkResponse(responseDelete, null)
        checkResponse(responseTrace, null)
        checkResponse(responseOptions, null)
        checkResponse(responsePatch, null)
    }

    @Test fun `HTTP methods with body work ok`() {

        val defaultFormat = serializationFormats.first()

        // bodyRequests
        val body = mapOf("key" to "value")
        val serializedBody = body.serialize(defaultFormat)

        val responseGet = client.get("/", body = serializedBody)
        val responsePost = client.post("/", serializedBody)
        val responsePut = client.put("/", serializedBody)
        val responseDelete = client.delete("/", serializedBody)
        val responseTrace = client.trace("/", serializedBody)
        val responseOptions = client.options("/", serializedBody)
        val responsePatch = client.patch("/", serializedBody)
        // bodyRequests

        checkResponse(responseGet, body)
        checkResponse(responsePost, body)
        checkResponse(responsePut, body)
        checkResponse(responseDelete, body)
        checkResponse(responseTrace, body)
        checkResponse(responseOptions, body)
        checkResponse(responsePatch, body)
    }

    @Test fun `HTTP methods with body and content type work ok`() {

        // bodyAndContentTypeRequests
        val body = mapOf("key" to "value")
        val serializedBody = body.serialize(APPLICATION_YAML)
        val yaml = ContentType(APPLICATION_YAML)

        val responseGet = client.get("/", body = serializedBody, contentType = yaml)
        val responsePost = client.post("/", serializedBody, contentType = yaml)
        val responsePut = client.put("/", serializedBody, contentType = yaml)
        val responseDelete = client.delete("/", serializedBody, contentType = yaml)
        val responseTrace = client.trace("/", serializedBody, contentType = yaml)
        val responseOptions = client.options("/", serializedBody, contentType = yaml)
        val responsePatch = client.patch("/", serializedBody, contentType = yaml)
        // bodyAndContentTypeRequests

        checkResponse(responseGet, body, yaml)
        checkResponse(responsePost, body, yaml)
        checkResponse(responsePut, body, yaml)
        checkResponse(responseDelete, body, yaml)
        checkResponse(responseTrace, body, yaml)
        checkResponse(responseOptions, body, yaml)
        checkResponse(responsePatch, body, yaml)
    }

    @Test fun `Parameters are set properly` () {
        val clientHeaders = Headers(Header("header1", "val1", "val2"))
        val settings = HttpClientSettings(
            baseUrl = server.binding,
            contentType = ContentType(APPLICATION_JSON),
            useCookies = false,
            headers = clientHeaders,
            insecure = true
        )
        val c = HttpClient(clientAdapter(), settings)

        assertEquals(c.settings.contentType, ContentType(APPLICATION_JSON))
        assert(!c.settings.useCookies)
        assertEquals(c.settings.headers, clientHeaders)

        callback = {
            val headers = Header("head1", request.headers.require("header1").strings())
            ok(headers = response.headers + headers)
        }

        c.use {
            it.start()
            it.get("/auth").apply {
                assertEquals(listOf("val1", "val2"), headers["head1"]?.values)
                assertEquals(status, OK_200)
            }
            it.get().apply {
                assertEquals(listOf("val1", "val2"), headers["head1"]?.values)
                assertEquals(status, OK_200)
            }
        }

        assertFalse(c.started())

        c.request {
            assertTrue(c.started())
            get("/auth").apply {
                assertEquals(listOf("val1", "val2"), headers["head1"]?.values)
                assertEquals(status, OK_200)
            }
        }

        c.start()
        assertTrue(c.started())

        c.request {
            assertTrue(c.started())
            get("/auth").apply {
                assertEquals(listOf("val1", "val2"), headers["head1"]?.values)
                assertEquals(status, OK_200)
            }
        }
    }

    @Test fun `Integers are sent properly` () {
        var run: Boolean

        callback = {
            val contentType = ContentType(APPLICATION_JSON, charset = Charsets.UTF_8)
            val number = BigInteger(request.body as ByteArray).toLong()
            ok(
                body = number,
                headers = response.headers + Header("body", number),
                contentType = contentType
            )
        }

        client.post("/string", 42).apply {
            assertEquals("42", headers.require("body").value)
            assertEquals(status, OK_200)
            run = true
        }

        assert(run)
    }

    @Test fun `Strings are sent properly` () {
        var run: Boolean

        client.post("/string", "text").apply {
            assert(headers["body"]?.string()?.isNotEmpty() ?: false)
            assertEquals(status, OK_200)
            run = true
        }

        assert(run)
    }

    private fun checkResponse(
        response: HttpResponsePort,
        parameter: Map<String, String>?,
        format: ContentType = ContentType(APPLICATION_JSON),
    ) {

        assertEquals(OK_200, response.status)
        assertEquals(
            parameter?.serialize(format.mediaType)?.trim() ?: "",
            response.bodyString().trim()
        )
    }
}
