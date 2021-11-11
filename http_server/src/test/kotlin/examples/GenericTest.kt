package com.hexagonkt.http.server.examples

import com.hexagonkt.http.Method
import com.hexagonkt.http.Method.GET
import com.hexagonkt.http.Path
import com.hexagonkt.http.client.Client
import com.hexagonkt.http.client.Request
import com.hexagonkt.http.client.Response
import com.hexagonkt.http.client.ahc.AhcAdapter
import com.hexagonkt.http.server.Call
import com.hexagonkt.http.server.Server
import com.hexagonkt.http.server.ServerPort
import com.hexagonkt.serialization.json.JacksonMapper
import com.hexagonkt.serialization.json.Json
import com.hexagonkt.core.serialization.SerializationManager
import com.hexagonkt.core.serialization.toObject
import com.hexagonkt.core.serialization.parse
import org.junit.jupiter.api.AfterAll
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestInstance
import org.junit.jupiter.api.TestInstance.Lifecycle.PER_CLASS
import java.io.File
import java.net.URL
import kotlin.text.Charsets.UTF_8

@TestInstance(PER_CLASS)
abstract class GenericTest(adapter: ServerPort) {

    private data class Tag(
        val id: String = System.currentTimeMillis().toString(),
        val name: String
    )

    private val directory = File("site/assets").let {
        if (it.exists()) it.path
        else "../site/assets"
    }

    private val part = "param"

    private val server: Server = Server(adapter) {
        before { response.headers["before"] = "filter" }

        get("/request/body") {
            val tag = request.body<Tag>()
            val tags = request.bodyObjects<Tag>()
            val tagMap = request.body<Map<String, *>>()
            val tagsMaps = request.bodyObjects<Map<String, *>>()

            assert(tags.first() == tag)
            assert(tagMap.toObject(Tag::class) == tag)
            assert(tagsMaps.first() == tagMap)
            assert(requestType == requestFormat.contentType)

            response.headers["requestOrigin"] = request.origin
            response.headers["requestUserAgent"] = request.userAgent
            response.headers["requestAccept"] = request.acceptValues

            ok(tag.copy(name = "${tag.name} processed"), charset = UTF_8)
        }

        get("/request/data") {
            response.headers["method"] = request.method.toString()
            response.headers["ip"] = request.ip
            response.headers["uri"] = request.url
            response.headers["queryParams"] = queryParametersValues.size.toString()
            response.headers["formParams"] = formParametersValues.size.toString()

            response.headers["agent"] = request.userAgent
            response.headers["scheme"] = request.scheme
            response.headers["host"] = request.host
            response.headers["query"] = request.queryString
            response.headers["port"] = request.port.toString()

            response.headers["secure"] = request.secure.toString()
            response.headers["referer"] = request.referer
            response.headers["preferredType"] = request.preferredType
            response.headers["accept"] = request.acceptValues.joinToString(",")
            response.headers["contentLength"] = request.contentLength.toString()
            response.headers["origin"] = request.origin

            ok("${request.url}!!!")
        }

        get("/request/queryParameters") {
            response.headers["queryParams"] = queryParameters.entries.joinToString(",") {
                "${it.key}:${it.value}"
            }
        }

        delete("/method") { okRequestMethod() }
        options("/method") { okRequestMethod() }
        get("/method") { okRequestMethod() }
        patch("/method") { okRequestMethod() }
        post("/method") { okRequestMethod() }
        put("/method") { okRequestMethod() }
        trace("/method") { okRequestMethod() }
        head("/method") { okRequestMethod() }

        get("/response/status") { send(201) }
        get("/response/body") { ok("body") }
        get("/response/pair") { send(202, "funky status") }
        get("/response/list") { ok(listOf("alpha", "beta")) }
        get("/response/map") { ok(mapOf("alpha" to 0, "beta" to true)) }
        get("/response/object") { ok(Tag(name = "Message")) }
        get("/response/pair/list") { send(201, listOf("alpha", "beta")) }
        get("/response/pair/map") { send(201, mapOf("alpha" to 0, "beta" to true)) }
        get("/response/pair/object") { send(201, Tag(name = "Message")) }

        get("/response/headers") {
            response.headers["unknown"] = null
            response.headers["unknown"] = "known"
            response.headers["unknown"] = null
            ok(response.headers["unknown"] ?: "unknown")
        }

        get("/") { ok("Hello Root!") }
        get("/redirect") { redirect("http://example.com") }

        get("/content/type") {
            val headerResponseType = request.headers["responseType"]

            if (headerResponseType != null)
                response.contentType = headerResponseType

            ok(responseType)
        }

        get("/param/{param}") { ok("echo: ${pathParameters["param"]}") }
        get("/paramwithmaj/{paramWithMaj}") { ok("echo: ${pathParameters["paramWithMaj"]}") }
        get("/tworoutes/$part/{param}") { ok("$part route: ${pathParameters["param"]}") }
        get("/tworoutes/${part.uppercase()}/{param}") {
            ok("${part.uppercase()} route: ${pathParameters["param"]}")
        }

        get(File(directory))
    }

    private val client: Client by lazy {
        Client(AhcAdapter(), "http://localhost:${server.runtimePort}")
    }

    @BeforeAll fun initialize() {
        SerializationManager.formats = linkedSetOf(Json)
        SerializationManager.mapper = JacksonMapper
        server.start()
    }

    @AfterAll fun shutdown() {
        server.stop()
    }

    @Test fun `Request body is parsed properly`() {
        val tag = Tag("id", "name")
        val headers = mapOf(
            "Accept" to listOf(Json.contentType),
            "Origin" to listOf("origin"),
            "User-Agent" to listOf("AHC")
        )
        val response = client.send(
            Request(GET, Path("/request/body"), tag, headers, contentType = Json.contentType)
        )
        assert(response.status == 200)
        assert("origin" == response.headers["requestOrigin"]?.first())
        assert(response.contentType == "${Json.contentType};charset=utf-8")
        assert(response.body?.parse(Tag::class) == tag.copy(name = "${tag.name} processed"))
    }

    @Test fun `Empty query string is handled properly`() {
        val response = client.get("/request/data", mapOf("Accept" to listOf("text/plain")))
        val port = URL(client.endpoint).port.toString()
        val host = response.headers["Host"]?.first()
        val ip = response.headers["ip"]?.first()
        val protocol = "http"

        assert("text/plain" == response.headers["Accept"]?.first())
        assert("AHC/2.1" == response.headers["agent"]?.first())
        assert(protocol == response.headers["scheme"]?.first())
        assert("127.0.0.1" == host || "localhost" == host)
        assert("127.0.0.1" == ip)
        assert("" == response.headers["query"]?.first())
        assert(port == response.headers["port"]?.first())
        assert("0" == response.headers["queryParams"]?.first())
        assert("0" == response.headers["formParams"]?.first())
        assert(null == response.headers["origin"]?.first())

        assert("false" == response.headers["secure"]?.first())
        assert(response.headers["referer"] == null)
        assert("text/plain" == response.headers["preferredType"]?.first())
        assert(response.headers["contentLength"]?.first()?.isNotEmpty() ?: false)

        assert(response.body == "$protocol://localhost:$port/request/data!!!")
        assert(200 == response.status)
    }

    @Test fun `Query string with no value parameters is handled properly`() {
        client.get("/request/queryParameters?a").apply {
            assert(headers["queryParams"]?.first()?.contains("a:") ?: false)
            assert(200 == status)
        }
        client.get("/request/queryParameters?f=1&a&b=").apply {
            assert(headers["queryParams"]?.first()?.contains("f:1") ?: false)
            assert(headers["queryParams"]?.first()?.contains("a:") ?: false)
            assert(headers["queryParams"]?.first()?.contains("b:") ?: false)
            assert(200 == status)
        }
    }

    @Test fun `Request data is read properly`() {
        val response = client.get ("/request/data?query")
        val port = URL(client.endpoint).port.toString ()
        val host = response.headers["Host"]?.first()
        val ip = response.headers["ip"]?.first()
        val protocol = "http"

        assert("AHC/2.1" == response.headers["agent"]?.first())
        assert(protocol == response.headers["scheme"]?.first())
        assert("127.0.0.1" == host || "localhost" == host)
        assert("127.0.0.1" == ip)
        assert("query" == response.headers["query"]?.first())
        assert(port == response.headers["port"]?.first())
        assert("1" == response.headers["queryParams"]?.first())
        assert("0" == response.headers["formParams"]?.first())
        assert(null == response.headers["origin"]?.first())

        assert("false" == response.headers["secure"]?.first())
        assert(response.headers["referer"] == null)
        assert("*/*" == response.headers["preferredType"]?.first())
        assert(response.headers["contentLength"]?.isNotEmpty() ?: false)

        assert(response.body == "$protocol://localhost:$port/request/data!!!")
        assert(200 == response.status)
    }

    @Test fun `HTTP methods are handled correctly`() {
        checkMethod(client, "HEAD")
        checkMethod(client, "DELETE")
        checkMethod(client, "OPTIONS")
        checkMethod(client, "GET")
        checkMethod(client, "PATCH")
        checkMethod(client, "POST")
        checkMethod(client, "PUT")
        checkMethod(client, "TRACE")
    }

    @Test fun `Response data is generated properly`() {
        assertResponseContains(client.get ("/response/status"), 201)
        assertResponseEquals(client.get ("/response/body"), "body")
        assertResponseEquals(client.get ("/response/pair"), "funky status", 202)
        assertResponseContains(client.get ("/response/list"), "alpha", "beta")
        assertResponseContains(client.get ("/response/map"), "alpha", "beta", "0", "true")
        assertResponseContains(client.get ("/response/object"), "id", "name", "Message")
        assertResponseContains(client.get ("/response/pair/list"), 201, "alpha", "beta")
        assertResponseContains(client.get ("/response/pair/map"), 201, "alpha", "beta", "0", "true")
        assertResponseContains(client.get ("/response/pair/object"), 201, "id", "name", "Message")
        assertResponseEquals(client.get ("/response/headers"), "unknown", 200)
    }

    @Test fun getRoot() {
        val response = client.get ("/")
        assertResponseEquals(response, "Hello Root!")
    }

    @Test fun `Root files content type is returned properly`() {
        val responseFile = client.get("/css/mkdocs.css")
        assert(responseFile.contentType?.contains("css") ?: false)
        assert(responseFile.status == 200)
        assert(responseFile.body?.contains("article") ?: false)
    }

    @Test fun echoParamWithUpperCaseInValue() {
        val camelCased = "ThisIsAValueAndBlackSheepShouldRetainItsUpperCasedCharacters"
        val response = client.get ("/param/$camelCased")
        assertResponseEquals(response, "echo: $camelCased")
    }

    @Test fun twoRoutesWithDifferentCase() {
        var expected = "expected"
        val response1 = client.get ("/tworoutes/$part/$expected")
        assertResponseEquals(response1, "$part route: $expected")

        expected = expected.uppercase()
        val response = client.get ("/tworoutes/${part.uppercase()}/$expected")
        assertResponseEquals(response, "${part.uppercase()} route: $expected")
    }

    @Test fun echoParamWithMaj() {
        val response = client.get ("/paramwithmaj/plop")
        assertResponseEquals(response, "echo: plop")
    }

    @Test fun notFound() {
        val response = client.get ("/no/resource")
        assertResponseContains(response, 404)
    }

    @Test fun redirect() {
        val response = client.get ("/redirect")
        assert(response.status == 302)
        assert(response.headers["Location"]?.first() == "http://example.com")
    }

    @Test fun requestDataWithDifferentHeaders() {
        val response = client.get ("/request/data?query", linkedMapOf(
            "Referer" to listOf("/"),
            "User-Agent" to listOf("ua")
        ))

        assert("ua" == response.headers["agent"]?.first())
        assert("/" == response.headers["Referer"]?.first())

        assert(200 == response.status)
    }

    @Test fun contentType () {
        fun contentType(vararg params: Pair<String, String>) = client.get(
            "/content/type",
            params.associate { it.first to listOf(it.second) }
        )
        .body

        assert(contentType("responseType" to "application/yaml") == "application/yaml")
        assert(contentType("Accept" to "text/plain") == "text/plain")
        // Check start because http client adds encoding
        assert(contentType("Content-Type" to "text/html")?.startsWith("text/html") ?: false)
        assert(contentType() == "application/json")
    }

    private fun Call.okRequestMethod() {
        response.headers["method"] = request.method.toString()
    }

    private fun checkMethod (client: Client, methodName: String) {
        val response = client.send(Request(Method.valueOf (methodName), Path("/method")))
        assert(response.headers["method"]?.first() == methodName)
        assert(response.headers["before"]?.first() == "filter")
        assert(200 == response.status)
    }

    private fun assertResponseEquals(response: Response<String>?, content: String, status: Int = 200) {
        assert(response?.headers?.get("before")?.first() == "filter")
        assert(response?.status == status)
        assert(response?.body == content)
    }

    private fun assertResponseContains(response: Response<String>?, status: Int, vararg content: String) {
        assert(response?.headers?.get("before")?.first() == "filter")
        assert(response?.status == status)
        content.forEach {
            assert(response?.body?.contains (it) ?: false)
        }
    }

    private fun assertResponseContains(response: Response<String>?, vararg content: String) {
        assertResponseContains(response, 200, *content)
    }
}
