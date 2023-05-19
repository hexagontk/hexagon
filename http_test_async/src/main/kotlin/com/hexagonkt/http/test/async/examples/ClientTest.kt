package com.hexagonkt.http.test.async.examples

import com.hexagonkt.core.require
import com.hexagonkt.core.media.APPLICATION_JSON
import com.hexagonkt.core.media.APPLICATION_YAML
import com.hexagonkt.handlers.async.done
import com.hexagonkt.http.SslSettings
import com.hexagonkt.http.client.HttpClient
import com.hexagonkt.http.client.HttpClientPort
import com.hexagonkt.http.client.HttpClientSettings
import com.hexagonkt.http.model.HttpRequest
import com.hexagonkt.http.model.HttpResponsePort
import com.hexagonkt.http.formatQueryString
import com.hexagonkt.http.model.*
import com.hexagonkt.http.model.HttpMethod.GET
import com.hexagonkt.http.model.HttpProtocol.HTTPS
import com.hexagonkt.http.model.INTERNAL_SERVER_ERROR_500
import com.hexagonkt.http.model.OK_200
import com.hexagonkt.http.server.async.serve
import com.hexagonkt.http.server.async.*
import com.hexagonkt.http.handlers.async.HttpCallback
import com.hexagonkt.http.handlers.async.HttpHandler
import com.hexagonkt.http.handlers.async.path
import com.hexagonkt.http.model.HttpMethod.PUT
import com.hexagonkt.http.server.async.HttpServerSettings
import com.hexagonkt.http.test.async.BaseTest
import com.hexagonkt.serialization.SerializationFormat
import com.hexagonkt.serialization.SerializationManager
import com.hexagonkt.serialization.serialize
import org.junit.jupiter.api.*

import java.math.BigInteger
import java.net.URL
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNull
import kotlin.test.assertTrue

@Suppress("FunctionName") // This class's functions are intended to be used only in tests
abstract class ClientTest(
    final override val clientAdapter: () -> HttpClientPort,
    final override val serverAdapter: () -> HttpServerPort,
    private val serializationFormats: List<SerializationFormat>,
    final override val serverSettings: HttpServerSettings = HttpServerSettings(),
) : BaseTest() {

    private var callback: HttpCallback = { this.done() }

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
        SerializationManager.defaultFormat = serializationFormats.firstOrNull()
    }

    @BeforeEach fun resetHandler() {
        callback = {
            val contentType = ContentType(APPLICATION_JSON, charset = Charsets.UTF_8)
            val bodyString = request.bodyString()
            val bodyHeader = if (bodyString.endsWith("\n") || bodyString.contains("{")) "json" else bodyString
            ok(
                body = bodyString,
                headers = response.headers
                    + Header("body", bodyHeader)
                    + Header("ct", request.contentType?.text ?: "")
                    + Header("query-parameters", formatQueryString(queryParameters)),
                contentType = contentType,
            ).done()
        }
    }

    @Test fun `Exceptions are returned as internal server errors`() {
        callback = { error("failure") }

        val response = client.send(HttpRequest())

        assertEquals(INTERNAL_SERVER_ERROR_500, response.status)
        assertTrue(response.bodyString().contains("failure"))
    }

    @Test fun `Form parameters are sent correctly`() {
        callback = {
            val headers = Headers(
                formParameters.httpFields.map { (k, v) -> Header(k, v.values) }
            )
            ok(headers = headers).done()
        }

        val response = client.send(
            HttpRequest(
                method = PUT,
                formParameters = FormParameters(
                    FormParameter("p1", "v11"),
                    FormParameter("p2", "v21", "v22"),
                )
            )
        )

        val expectedHeaders = Headers(Header("p1", "v11"), Header("p2", "v21", "v22"))
        val actualHeaders = response.headers - "transfer-encoding" - "content-length" - "connection"
        assertEquals(expectedHeaders, actualHeaders)
    }

    @Test fun `Cookies are sent correctly`() {
        callback = {
            val cookiesMap = request.cookiesMap()
            val actual = cookiesMap.require("c1")
            val expected = Cookie("c1", "v1", httpOnly = false)
            assertEquals(expected, actual)
            assertEquals(Cookie("c2", "v2", -1, httpOnly = false), cookiesMap["c2"])
            assertNull(cookiesMap["c3"]) // Secure headers only sent through HTTPS
            ok(cookies = listOf(
                Cookie("c4", "v4", 60),
                Cookie("c5", "v5", secure = true),
            )).done()
        }

        client.cookies = emptyList()
        val response = client.send(
            HttpRequest(
                cookies = listOf(
                    Cookie("c1", "v1"),
                    Cookie("c2", "v2", 1),
                    Cookie("c3", "v3", secure = true),
                )
            )
        )

        val responseC4 = response.cookiesMap().require("c4")
        assertEquals("v4", responseC4.value)
        assertTrue(responseC4.maxAge in 59..60)
        assertEquals(Cookie("c5", "v5", secure = true), response.cookiesMap()["c5"])

        val clientC4 = client.cookiesMap().require("c4")
        assertEquals("v4", clientC4.value)
        assertTrue(clientC4.maxAge in 59..60)
        assertEquals(Cookie("c5", "v5", secure = true), client.cookiesMap()["c5"])
    }

    @Test fun `Create HTTP clients`() {
        val adapter = clientAdapter()

        // clientCreation
        HttpClient(adapter)
        HttpClient(adapter, HttpClientSettings(URL("http://host:1234/base")))
        // clientCreation

        // clientSettingsCreation
        // All client settings parameters are optionals and provide default values
        HttpClient(adapter, HttpClientSettings(
            baseUrl = URL("http://host:1234/base"),
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

        // genericRequest
        val request = HttpRequest(
            method = GET,
            path = "/",
            body = mapOf("body" to "payload").serialize(),
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

        // bodyRequests
        val body = mapOf("key" to "value")
        val serializedBody = body.serialize()

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
            baseUrl = URL("http://localhost:${server.runtimePort}"),
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
            val headers = Header("head1", request.headers.require("header1").values)
            ok(headers = response.headers + headers).done()
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
            ).done()
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
            assert(headers["body"]?.value?.isNotEmpty() ?: false)
            assertEquals(status, OK_200)
            run = true
        }

        assert(run)
    }

    @Test fun `Request HTTPS example`() {

        val serverAdapter = serverAdapter()

        // Key store files
        val identity = "hexagonkt.p12"
        val trust = "trust.p12"

        // Default passwords are file name reversed
        val keyStorePassword = identity.reversed()
        val trustStorePassword = trust.reversed()

        // Key stores can be set as URIs to classpath resources (the triple slash is needed)
        val keyStore = URL("classpath:ssl/$identity")
        val trustStore = URL("classpath:ssl/$trust")

        val sslSettings = SslSettings(
            keyStore = keyStore,
            keyStorePassword = keyStorePassword,
            trustStore = trustStore,
            trustStorePassword = trustStorePassword,
            clientAuth = true // Requires a valid certificate from the client (mutual TLS)
        )

        val serverSettings = serverSettings.copy(
            bindPort = 0,
            protocol = HTTPS, // You can also use HTTP2
            sslSettings = sslSettings
        )

        val server = serve(serverAdapter, serverSettings) {
            get("/hello") {
                // We can access the certificate used by the client from the request
                val subjectDn = request.certificate()?.subjectX500Principal?.name ?: ""
                ok("Hello World!", headers = response.headers + Header("cert", subjectDn)).done()
            }
        }

        // We'll use the same certificate for the client (in a real scenario it would be different)
        val clientSettings = HttpClientSettings(
            baseUrl = URL("https://localhost:${server.runtimePort}"),
            sslSettings = sslSettings
        )

        // Create an HTTP client and make an HTTPS request
        val client = HttpClient(clientAdapter(), clientSettings)
        client.start()
        client.get("/hello").apply {
            // Assure the certificate received (and returned) by the server is correct
            assert(headers.require("cert").value?.startsWith("CN=hexagonkt.com") ?: false)
            assertEquals(body, "Hello World!")
        }

        client.stop()
        server.stop()
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
