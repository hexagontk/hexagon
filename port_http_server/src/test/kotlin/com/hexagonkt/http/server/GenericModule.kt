package com.hexagonkt.http.server

import com.hexagonkt.http.Method
import com.hexagonkt.http.client.Client
import java.net.URL
import java.util.Locale.getDefault as defaultLocale

@Suppress("unused", "MemberVisibilityCanPrivate") // Test methods are flagged as unused
internal class GenericModule : TestModule() {
    internal class CustomException : IllegalArgumentException()

    internal data class Tag(
        val id: String = System.currentTimeMillis().toString(),
        val name: String
    )

    private val part = "param"

    private val fortuneMessages = setOf(
        "fortune: No such file or directory",
        "A computer scientist is someone who fixes things that aren't broken.",
        "After enough decimal places, nobody gives a damn.",
        "A bad random number generator: 1, 1, 1, 1, 1, 4.33e+67, 1, 1, 1",
        "A computer program does what you tell it to do, not what you want it to do.",
        "Emacs is a nice operating system, but I prefer UNIX. — Tom Christaensen",
        "Any program that runs right is obsolete.",
        "A list is only as strong as its weakest link. — Donald Knuth",
        "Feature: A bug with seniority.",
        "Computers make very fast, very accurate mistakes.",
        "<script>alert(\"This should not be displayed in a browser alert box.\");</script>",
        "フレームワークのベンチマーク"
    )

    override fun initialize(): Router = Router {
        before("/protected/*") { halt(401, "Go Away!") }
        before("/attribute") { attributes += "attr1" to "attr" }

        assets("public")

        get("/request/data") {
            response.body = request.url

            request.cookies["method"]?.value = request.method.toString()
            request.cookies["host"]?.value = request.ip
            request.cookies["uri"]?.value = request.url
            request.cookies["params"]?.value = request.parameters.size.toString()

            response.addHeader("method", request.method.toString())
            response.addHeader("ip", request.ip)
            response.addHeader("uri", request.url)
            response.addHeader("params", request.parameters.size.toString())

            response.addHeader("agent", request.userAgent)
            response.addHeader("scheme", request.scheme)
            response.addHeader("host", request.host)
            response.addHeader("query", request.queryString)
            response.addHeader("port", request.port.toString())

            response.addHeader("secure", request.secure.toString())
            response.addHeader("referer", request.referer)
            response.addHeader("preferredType", request.preferredType)
            response.addHeader("contentLength", request.contentLength.toString())

            ok ("${response.body}!!!")
        }

        error(UnsupportedOperationException::class) {
            response.addHeader("error", it.message ?: it.javaClass.name)
            send(599, "Unsupported")
        }

        error(IllegalArgumentException::class) {
            response.addHeader("runtimeError", it.message ?: it.javaClass.name)
            send(598, "Runtime")
        }

        get("/exception") { throw UnsupportedOperationException("error message") }
        get("/baseException") { throw CustomException() }
        get("/unhandledException") { error("error message") }
        get("/hi") { ok ("Hello World!") }
        get("/param/{param}") { ok ("echo: ${request.singleParameters["param"]}") }
        get("/paramwithmaj/{paramWithMaj}") { ok ("echo: ${request.singleParameters["paramWithMaj"]}") }
        get("/") { ok("Hello Root!") }
        post("/poster") { send(201, "Body was: ${request.body}") }
        patch("/patcher") { ok ("Body was: ${request.body}") }
        delete ("/method") { okRequestMethod () }
        options ("/method") { okRequestMethod () }
        get ("/method") { okRequestMethod () }
        patch ("/method") { okRequestMethod () }
        post ("/method") { okRequestMethod () }
        put ("/method") { okRequestMethod () }
        trace ("/method") { okRequestMethod () }
        head ("/method") { response.addHeader ("header", request.method.toString()) }
        get("/halt") { halt("halted") }
        get("/tworoutes/$part/{param}") { ok ("$part route: ${request.singleParameters["param"]}") }

        get("/tworoutes/${part.toUpperCase()}/{param}") {
            ok ("${part.toUpperCase()} route: ${request.singleParameters["param"]}")
        }

        get("/reqres") { ok (request.method) }
        get("/redirect") { redirect("http://example.com") }
        get("/attribute") { ok(attributes["attr1"] ?: "not found") }
        get("/content/type") {
            val headerResponseType = request.singleHeaders["responseType"]

            if (headerResponseType != null)
                response.contentType = headerResponseType

            ok(responseType)
        }

        after("/hi") {
            response.addHeader ("after", "foobar")
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
    }

    private fun Call.okRequestMethod() = ok(request.method)

    fun reqres(client: Client) {
        val response = client.get("/reqres")
        assertResponseEquals(response, "GET")
    }

    fun getHi(client: Client) {
        val response = client.get("/hi")
        assertResponseEquals(response, "Hello World!")
    }

    fun getHiAfterFilter(client: Client) {
        val response = client.get ("/hi")
        assertResponseEquals(response, "Hello World!")
        assert(response.headers["after"]?.contains("foobar") ?: false)
    }

    fun getRoot(client: Client) {
        val response = client.get ("/")
        assertResponseEquals(response, "Hello Root!")
    }

    fun echoParam1(client: Client) {
        val response = client.get ("/param/shizzy")
        assertResponseEquals(response, "echo: shizzy")
    }

    fun echoParam2(client: Client) {
        val response = client.get ("/param/gunit")
        assertResponseEquals(response, "echo: gunit")
    }

    fun echoParamWithUpperCaseInValue(client: Client) {
        val camelCased = "ThisIsAValueAndBlacksheepShouldRetainItsUpperCasedCharacters"
        val response = client.get ("/param/" + camelCased)
        assertResponseEquals(response, "echo: $camelCased")
    }

    fun twoRoutesWithDifferentCase(client: Client) {
        var expected = "expected"
        val response1 = client.get ("/tworoutes/$part/$expected")
        assertResponseEquals(response1, "$part route: $expected")

        expected = expected.toUpperCase()
        val response = client.get ("/tworoutes/${part.toUpperCase()}/$expected")
        assertResponseEquals(response, "${part.toUpperCase()} route: $expected")
    }

    fun echoParamWithMaj(client: Client) {
        val response = client.get ("/paramwithmaj/plop")
        assertResponseEquals(response, "echo: plop")
    }

    fun unauthorized(client: Client) {
        val response = client.get ("/protected/resource")
        assert(response.statusCode == 401)
    }

    fun notFound(client: Client) {
        val response = client.get ("/no/resource")
        assertResponseContains(response, 404)
    }

    fun postOk(client: Client) {
        val response = client.post ("/poster", "Fo shizzy")
        assertResponseContains(response, 201, "Fo shizzy")
    }

    fun patchOk(client: Client) {
        val response = client.patch ("/patcher", "Fo shizzy")
        assertResponseContains(response, "Fo shizzy")
    }

    fun staticFolder(client: Client) {
        val response = client.get ("/file.txt/")
        assertResponseContains(response, 404)
    }

    fun staticFile(client: Client) {
        val response = client.get ("/file.txt")
        assertResponseEquals(response, "file content\n")
    }

    fun fileContentType(client: Client) {
        val response = client.get ("/file.css")
        assert(response.contentType.contains("css"))
        assertResponseEquals(response, "/* css */\n")
    }

    fun halt(client: Client) {
        val response = client.get ("/halt")
        assertResponseEquals(response, "halted", 500)
    }

    fun redirect(client: Client) {
        val response = client.get ("/redirect")
        assert(response.statusCode == 302)
        assert(response.headers["Location"] == "http://example.com")
    }

    fun requestData(client: Client) {
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

    fun requestDataWithDifferentHeaders(client: Client) {
        val response = client.get ("/request/data?query", linkedMapOf(
            "Referer" to listOf("/"),
            "User-Agent" to listOf("ua")
        ))

        assert("ua" == response.headers["agent"])
        assert("/" == response.headers["referer"])

        assert(200 == response.statusCode)
    }

    fun handleException(client: Client) {
        val response = client.get ("/exception")
        assert("error message" == response.headers["error"]?.toString())
        assertResponseContains(response, 599, "Unsupported")
    }

    fun base_error_handler(client: Client) {
        val response = client.get ("/baseException")
        assert(response.headers["runtimeError"]?.toString() == CustomException::class.java.name)
        assertResponseContains(response, 598, "Runtime")
    }

    fun not_registered_error_handler(client: Client) {
        val response = client.get ("/unhandledException")
        assertResponseContains(response, 500)
    }

    fun return_values (client: Client) {
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

    fun methods (client: Client) {
        checkMethod (client, "HEAD", "header") // Head does not support body message
        checkMethod (client, "DELETE")
        checkMethod (client, "OPTIONS")
        checkMethod (client, "GET")
        checkMethod (client, "PATCH")
        checkMethod (client, "POST")
        checkMethod (client, "PUT")
        checkMethod (client, "TRACE")
    }

    fun contentType (client: Client) {
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

    fun attributes (client: Client) {
        assert(client.get("/attribute").responseBody == "attr")
    }

    private fun checkMethod (client: Client, methodName: String, headerName: String? = null) {
        val res = client.send(Method.valueOf (methodName), "/method")
        assert (
            if (headerName == null) res.responseBody != null
            else res.headers.get(headerName) == methodName
        )
        assert (200 == res.statusCode)
    }

    override fun validate(client: Client) {
        reqres(client)
        getHi(client)
        getHiAfterFilter(client)
        getRoot(client)
        echoParam1(client)
        echoParam2(client)
        echoParamWithUpperCaseInValue(client)
        twoRoutesWithDifferentCase(client)
        echoParamWithMaj(client)
        unauthorized(client)
        notFound(client)
        postOk(client)
        patchOk(client)
        staticFolder(client)
        staticFile(client)
        fileContentType(client)
        halt(client)
        redirect(client)
        requestData(client)
        handleException(client)
        base_error_handler(client)
        not_registered_error_handler(client)
        return_values (client)
        methods(client)
        attributes(client)
        contentType(client)
    }
}

