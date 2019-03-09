package com.hexagonkt.http.server.examples

import com.hexagonkt.http.Method
import com.hexagonkt.http.client.Client
import com.hexagonkt.http.server.Call
import com.hexagonkt.http.server.Server
import com.hexagonkt.http.server.ServerPort
import org.asynchttpclient.Response
import org.asynchttpclient.request.body.multipart.StringPart
import org.testng.annotations.AfterClass
import org.testng.annotations.BeforeClass
import org.testng.annotations.Test
import java.net.URL
import java.util.Locale.getDefault as defaultLocale

@Test abstract class GenericTest(adapter: ServerPort) {

    private data class Tag(
        val id: String = System.currentTimeMillis().toString(),
        val name: String
    )

    private val part = "param"

    private val server: Server by lazy {

        Server(adapter) {

            before("/protected/*") { halt(401, "Go Away!") }
            before("/attribute") { attributes += "attr1" to "attr" }

            assets("public")

            get("/request/data") {
                response.body = request.url

                request.cookies["method"]?.value = request.method.toString()
                request.cookies["host"]?.value = request.ip
                request.cookies["uri"]?.value = request.url
                request.cookies["params"]?.value = parameters.size.toString()

                response.setHeader("method", request.method.toString())
                response.setHeader("ip", request.ip)
                response.setHeader("uri", request.url)
                response.setHeader("params", parameters.size.toString())

                response.setHeader("agent", request.userAgent)
                response.setHeader("scheme", request.scheme)
                response.setHeader("host", request.host)
                response.setHeader("query", request.queryString)
                response.setHeader("port", request.port.toString())

                response.setHeader("secure", request.secure.toString())
                response.setHeader("referer", request.referer)
                response.setHeader("preferredType", request.preferredType)
                response.setHeader("contentLength", request.contentLength.toString())

                ok("${response.body}!!!")
            }

            get("/hi") { ok("Hello World!") }
            get("/param/{param}") { ok("echo: ${pathParameters["param"]}") }
            get("/paramwithmaj/{paramWithMaj}") { ok("echo: ${pathParameters["paramWithMaj"]}") }
            get("/") { ok("Hello Root!") }
            post("/poster") { send(201, "Body was: ${request.body}") }
            patch("/patcher") { ok("Body was: ${request.body}") }
            delete("/method") { okRequestMethod() }
            options("/method") { okRequestMethod() }
            get("/method") { okRequestMethod() }
            patch("/method") { okRequestMethod() }
            post("/method") { okRequestMethod() }
            put("/method") { okRequestMethod() }
            trace("/method") { okRequestMethod() }
            head("/method") { response.setHeader("header", request.method.toString()) }
            get("/tworoutes/$part/{param}") { ok("$part route: ${pathParameters["param"]}") }

            get("/tworoutes/${part.toUpperCase()}/{param}") {
                ok("${part.toUpperCase()} route: ${pathParameters["param"]}")
            }

            get("/reqres") { ok(request.method) }
            get("/redirect") { redirect("http://example.com") }
            get("/attribute") { ok(attributes["attr1"] ?: "not found") }
            get("/content/type") {
                val headerResponseType = request.headers["responseType"]?.first()

                if (headerResponseType != null)
                    response.contentType = headerResponseType

                ok(responseType)
            }

            after("/hi") {
                response.setHeader("after", "foobar")
            }

            get("/return/status") { send(201) }
            get("/return/body") { ok("body") }
            get("/return/pair") { send(202, "funky status") }
            get("/return/list") { ok(listOf("alpha", "beta")) }
            get("/return/map") { ok(mapOf("alpha" to 0, "beta" to true)) }
            get("/return/object") { ok(Tag(name = "Message")) }
            get("/return/pair/list") { send(201, listOf("alpha", "beta")) }
            get("/return/pair/map") { send(201, mapOf("alpha" to 0, "beta" to true)) }
            get("/return/pair/object") { send(201, Tag(name = "Message")) }

            post("/hexagon/files") { ok(request.parts.keys.joinToString(":")) }
        }
    }

    private val client: Client by lazy { Client("http://localhost:${server.runtimePort}") }

    @BeforeClass fun initialize() {
        server.start()
    }

    @AfterClass fun shutdown() {
        server.stop()
    }

    private fun Call.okRequestMethod() = ok(request.method)

    @Test fun reqres() {
        val response = client.get("/reqres")
        assertResponseEquals(response, "GET")
    }

    @Test fun getHi() {
        val response = client.get("/hi")
        assertResponseEquals(response, "Hello World!")
    }

    @Test fun getHiAfterFilter() {
        val response = client.get ("/hi")
        assertResponseEquals(response, "Hello World!")
        assert(response.headers["after"]?.contains("foobar") ?: false)
    }

    @Test fun getRoot() {
        val response = client.get ("/")
        assertResponseEquals(response, "Hello Root!")
    }

    @Test fun echoParam1() {
        val response = client.get ("/param/shizzy")
        assertResponseEquals(response, "echo: shizzy")
    }

    @Test fun echoParam2() {
        val response = client.get ("/param/gunit")
        assertResponseEquals(response, "echo: gunit")
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

    @Test fun unauthorized() {
        val response = client.get ("/protected/resource")
        assert(response.statusCode == 401)
    }

    @Test fun notFound() {
        val response = client.get ("/no/resource")
        assertResponseContains(response, 404)
    }

    @Test fun postOk() {
        val response = client.post ("/poster", "Fo shizzy")
        assertResponseContains(response, 201, "Fo shizzy")
    }

    @Test fun patchOk() {
        val response = client.patch ("/patcher", "Fo shizzy")
        assertResponseContains(response, "Fo shizzy")
    }

    @Test fun staticFolder() {
        val response = client.get ("/file.txt/")
        assertResponseContains(response, 404)
    }

    @Test fun staticFile() {
        val response = client.get ("/file.txt")
        assertResponseEquals(response, "file content\n")
    }

    @Test fun fileContentType() {
        val response = client.get ("/file.css")
        assert(response.contentType.contains("css"))
        assertResponseEquals(response, "/* css */\n")
    }

    @Test fun redirect() {
        val response = client.get ("/redirect")
        assert(response.statusCode == 302)
        assert(response.headers["Location"] == "http://example.com")
    }

    @Test fun requestData() {
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

        assert("false" == response.headers["secure"])
        assert("UNKNOWN" == response.headers["referer"])
        assert("text/plain" == response.headers["preferredType"])
        assert(response.headers["contentLength"].isNotEmpty())

        assert(response.responseBody == "$protocol://localhost:$port/request/data!!!")
        assert(200 == response.statusCode)
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

    @Test fun return_values () {
        assertResponseContains(client.get ("/return/status"), 201)
        assertResponseEquals(client.get ("/return/body"), "body")
        assertResponseEquals(client.get ("/return/pair"), "funky status", 202)
        assertResponseContains(client.get ("/return/list"), "alpha", "beta")
        assertResponseContains(client.get ("/return/map"), "alpha", "beta", "0", "true")
        assertResponseContains(client.get ("/return/object"), "id", "name", "Message")
        assertResponseContains(client.get ("/return/pair/list"), 201, "alpha", "beta")
        assertResponseContains(client.get ("/return/pair/map"), 201, "alpha", "beta", "0", "true")
        assertResponseContains(client.get ("/return/pair/object"), 201, "id", "name", "Message")
    }

    @Test fun methods () {
        checkMethod (client, "HEAD", "header") // Head does not support body message
        checkMethod (client, "DELETE")
        checkMethod (client, "OPTIONS")
        checkMethod (client, "GET")
        checkMethod (client, "PATCH")
        checkMethod (client, "POST")
        checkMethod (client, "PUT")
        checkMethod (client, "TRACE")
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

    @Test fun attributes () {
        assert(client.get("/attribute").responseBody == "attr")
    }

    @Test fun sendParts() {
        val parts = listOf(StringPart("name", "value"))
        val response = client.send(Method.POST, "/hexagon/files", parts = parts)
        assert(response.responseBody == "name")
    }

    private fun checkMethod (client: Client, methodName: String, headerName: String? = null) {
        val res = client.send(Method.valueOf (methodName), "/method")
        assert (
            if (headerName == null) res.responseBody != null
            else res.headers.get(headerName) == methodName
        )
        assert (200 == res.statusCode)
    }

    private fun assertResponseEquals(response: Response?, content: String, status: Int = 200) {
        assert (response?.statusCode == status)
        assert (response?.responseBody == content)
    }

    private fun assertResponseContains(response: Response?, status: Int, vararg content: String) {
        assert (response?.statusCode == status)
        content.forEach {
            assert (response?.responseBody?.contains (it) ?: false)
        }
    }

    private fun assertResponseContains(response: Response?, vararg content: String) {
        assertResponseContains(response, 200, *content)
    }
}

