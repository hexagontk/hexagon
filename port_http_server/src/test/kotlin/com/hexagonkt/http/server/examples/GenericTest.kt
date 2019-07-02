package com.hexagonkt.http.server.examples

import com.hexagonkt.http.Method
import com.hexagonkt.http.Method.GET
import com.hexagonkt.http.client.Client
import com.hexagonkt.http.server.Call
import com.hexagonkt.http.server.Server
import com.hexagonkt.http.server.ServerPort
import com.hexagonkt.serialization.Json
import com.hexagonkt.serialization.convertToObject
import com.hexagonkt.serialization.parse
import org.asynchttpclient.Response
import org.testng.annotations.AfterClass
import org.testng.annotations.BeforeClass
import org.testng.annotations.Test
import java.io.File
import java.net.URL
import kotlin.text.Charsets.UTF_8

@Test abstract class GenericTest(adapter: ServerPort) {

    private data class Tag(
        val id: String = System.currentTimeMillis().toString(),
        val name: String
    )

    private val directory = File("hexagon_site/assets").let {
        if (it.exists()) it.path
        else "../hexagon_site/assets"
    }

    private val part = "param"

    private val server: Server by lazy {
        Server(adapter) {
            before { response.setHeader("before", "filter") }

            get("/request/body") {
                val tag = request.bodyObject(Tag::class)
                val tags = request.bodyObjects(Tag::class)
                val tagMap = request.bodyObject()
                val tagsMaps = request.bodyObjects()

                assert(tags.first() == tag)
                assert(tagMap.convertToObject(Tag::class) == tag)
                assert(tagsMaps.first() == tagMap)
                assert(requestType == requestFormat.contentType)

                ok(tag.copy(name = "${tag.name} processed"), charset = UTF_8)
            }

            get("/request/data") {
                response.setHeader("method", request.method.toString())
                response.setHeader("ip", request.ip)
                response.setHeader("uri", request.url)
                response.setHeader("params", parameters.size.toString())
                response.setHeader("queryParams", queryParameters.size.toString())
                response.setHeader("formParams", formParameters.size.toString())

                response.setHeader("agent", request.userAgent)
                response.setHeader("scheme", request.scheme)
                response.setHeader("host", request.host)
                response.setHeader("query", request.queryString)
                response.setHeader("port", request.port.toString())

                response.setHeader("secure", request.secure.toString())
                response.setHeader("referer", request.referer)
                response.setHeader("preferredType", request.preferredType)
                response.setHeader("accept", request.accept.joinToString(","))
                response.setHeader("contentLength", request.contentLength.toString())

                ok("${request.url}!!!")
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

            get("/") { ok("Hello Root!") }
            get("/redirect") { redirect("http://example.com") }

            get("/content/type") {
                val headerResponseType = request.headers["responseType"]?.first()

                if (headerResponseType != null)
                    response.contentType = headerResponseType

                ok(responseType)
            }

            get("/param/{param}") { ok("echo: ${pathParameters["param"]}") }
            get("/paramwithmaj/{paramWithMaj}") { ok("echo: ${pathParameters["paramWithMaj"]}") }
            get("/tworoutes/$part/{param}") { ok("$part route: ${pathParameters["param"]}") }
            get("/tworoutes/${part.toUpperCase()}/{param}") {
                ok("${part.toUpperCase()} route: ${pathParameters["param"]}")
            }

            assets(File(directory))
        }
    }

    private val client: Client by lazy { Client("http://localhost:${server.runtimePort}") }

    @BeforeClass fun initialize() {
        server.start()
    }

    @AfterClass fun shutdown() {
        server.stop()
    }

    @Test fun `Request body is parsed properly`() {
        val tag = Tag("id", "name")
        val response = client.send(GET, "/request/body", tag, Json.contentType)
        assert(response.statusCode == 200)
        assert(response.contentType == "${Json.contentType};charset=utf-8")
        assert(response.responseBody.parse(Tag::class) == tag.copy(name = "${tag.name} processed"))
    }

    @Test fun `Empty query string is handled properly`() {
        val response = client.get("/request/data", mapOf("Accept" to listOf("text/plain")))
        val port = URL(client.endpoint).port.toString()
        val host = response.headers["host"]
        val ip = response.headers["ip"]
        val protocol = "http"

        assert("text/plain" == response.headers["accept"])
        assert("AHC/2.1" == response.headers["agent"])
        assert(protocol == response.headers["scheme"])
        assert("127.0.0.1" == host || "localhost" == host)
        assert("127.0.0.1" == ip || "localhost" == ip) // TODO Force IP
        assert("" == response.headers["query"])
        assert(port == response.headers["port"])
        assert("0" == response.headers["params"])
        assert("0" == response.headers["queryParams"])
        assert("0" == response.headers["formParams"])

        assert("false" == response.headers["secure"])
        assert(response.headers["referer"] == null)
        assert("text/plain" == response.headers["preferredType"])
        assert(response.headers["contentLength"].isNotEmpty())

        assert(response.responseBody == "$protocol://localhost:$port/request/data!!!")
        assert(200 == response.statusCode)
    }

    @Test fun `Request data is read properly`() {
        val response = client.get ("/request/data?query")
        val port = URL(client.endpoint).port.toString ()
        val host = response.headers["host"]
        val ip = response.headers["ip"]
        val protocol = "http"

        assert("AHC/2.1" == response.headers["agent"])
        assert(protocol == response.headers["scheme"])
        assert("127.0.0.1" == host || "localhost" == host)
        assert("127.0.0.1" == ip || "localhost" == ip) // TODO Force IP
        assert("query" == response.headers["query"])
        assert(port == response.headers["port"])
        assert("1" == response.headers["params"])
        assert("1" == response.headers["queryParams"])
        assert("0" == response.headers["formParams"])

        assert("false" == response.headers["secure"])
        assert(response.headers["referer"] == null)
        assert("*/*" == response.headers["preferredType"])
        assert(response.headers["contentLength"].isNotEmpty())

        assert(response.responseBody == "$protocol://localhost:$port/request/data!!!")
        assert(200 == response.statusCode)
    }

    @Test fun `HTTP methods are handled correctly`() {
        checkMethod (client, "HEAD")
        checkMethod (client, "DELETE")
        checkMethod (client, "OPTIONS")
        checkMethod (client, "GET")
        checkMethod (client, "PATCH")
        checkMethod (client, "POST")
        checkMethod (client, "PUT")
        checkMethod (client, "TRACE")
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
    }

    @Test fun getRoot() {
        val response = client.get ("/")
        assertResponseEquals(response, "Hello Root!")
    }

    @Test fun `Root files content type is returned properly`() {
        val responseFile = client.get("/css/mkdocs.css")
        assert(responseFile.contentType.contains("css"))
        assert(responseFile.statusCode == 200)
        assert(responseFile.responseBody.contains("article"))
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

        expected = expected.toUpperCase()
        val response = client.get ("/tworoutes/${part.toUpperCase()}/$expected")
        assertResponseEquals(response, "${part.toUpperCase()} route: $expected")
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
        assert(response.statusCode == 302)
        assert(response.headers["Location"] == "http://example.com")
    }

    @Test fun requestDataWithDifferentHeaders() {
        val response = client.get ("/request/data?query", linkedMapOf(
            "Referer" to listOf("/"),
            "User-Agent" to listOf("ua")
        ))

        assert("ua" == response.headers["agent"])
        assert("/" == response.headers["referer"])

        assert(200 == response.statusCode)
    }

    @Test fun contentType () {
        fun contentType(vararg params: Pair<String, String>) = client.get(
            "/content/type",
            params.map { it.first to listOf(it.second) }.toMap()
        )
        .responseBody

        assert(contentType("responseType" to "application/yaml") == "application/yaml")
        assert(contentType("Accept" to "text/plain") == "text/plain")
        // Check start because http client adds encoding
        assert(contentType("Content-Type" to "text/html").startsWith("text/html"))
        assert(contentType() == "application/json")
    }

    private fun Call.okRequestMethod() {
        response.setHeader("method", request.method.toString())
    }

    private fun checkMethod (client: Client, methodName: String) {
        val response = client.send(Method.valueOf (methodName), "/method")
        assert(response.headers.get("method") == methodName)
        assert(response.headers.get("before") == "filter")
        assert(200 == response.statusCode)
    }

    private fun assertResponseEquals(response: Response?, content: String, status: Int = 200) {
        assert(response?.headers?.get("before") == "filter")
        assert(response?.statusCode == status)
        assert(response?.responseBody == content)
    }

    private fun assertResponseContains(response: Response?, status: Int, vararg content: String) {
        assert(response?.headers?.get("before") == "filter")
        assert(response?.statusCode == status)
        content.forEach {
            assert(response?.responseBody?.contains (it) ?: false)
        }
    }

    private fun assertResponseContains(response: Response?, vararg content: String) {
        assertResponseContains(response, 200, *content)
    }
}
