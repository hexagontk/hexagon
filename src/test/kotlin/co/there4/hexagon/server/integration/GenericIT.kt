package co.there4.hexagon.server.integration

import co.there4.hexagon.client.Client
import co.there4.hexagon.template.KotlinxHtmlRenderer.page
import co.there4.hexagon.server.*
import kotlinx.html.*
import java.time.LocalDateTime
import java.util.Locale.getDefault as defaultLocale

import kotlin.test.assertTrue

@Suppress("unused") // Test methods are flagged as unused
class GenericIT : ItTest () {
    private val part = "param"

    private val FORTUNE_MESSAGES = setOf(
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

    override fun initialize(srv: Router) {
        srv.before("/protected/*") { halt(401, "Go Away!") }

        srv.get("/request/data") {
            response.body = request.url

            request.cookies["method"]?.value = request.method.toString()
            request.cookies["host"]?.value = request.ip
            request.cookies["uri"]?.value = request.url
            request.cookies["params"]?.value = request.parameters.size.toString()

            response.addHeader("agent", request.userAgent)
            response.addHeader("scheme", request.scheme)
            response.addHeader("host", request.host)
            response.addHeader("query", request.queryString)
            response.addHeader("port", request.port.toString())

            ok ("${response.body}!!!")
        }

        srv.error(UnsupportedOperationException::class) {
            response.addHeader("error", it.message ?: it.javaClass.name)
        }

        srv.get("/*") { pass() }
        srv.get("/exception") { throw UnsupportedOperationException("error message") }
        srv.get("/hi") { ok ("Hello World!") }
        srv.get("/param/{param}") { ok ("echo: ${request ["param"]}") }
        srv.get("/paramwithmaj/{paramWithMaj}") { ok ("echo: ${request ["paramWithMaj"]}") }
        srv.get("/") { ok("Hello Root!") }
        srv.post("/poster") { created("Body was: ${request.body}") }
        srv.patch("/patcher") { ok ("Body was: ${request.body}") }
        srv.delete ("/method") { okRequestMethod () }
        srv.options ("/method") { okRequestMethod () }
        srv.get ("/method") { okRequestMethod () }
        srv.patch ("/method") { okRequestMethod () }
        srv.post ("/method") { okRequestMethod () }
        srv.put ("/method") { okRequestMethod () }
        srv.trace ("/method") { okRequestMethod () }
        srv.head ("/method") { response.addHeader ("header", request.method.toString()) }
        srv.get("/halt") { halt("halted") }
        srv.get("/tworoutes/$part/{param}") { ok ("$part route: ${request ["param"]}") }
        srv.get("/template") {
            template("pebble_template.html", defaultLocale(), mapOf("date" to LocalDateTime.now()))
        }

        srv.get("/tworoutes/${part.toUpperCase()}/{param}") {
            ok ("${part.toUpperCase()} route: ${request ["param"]}")
        }

        srv.get("/reqres") { ok (request.method) }

        srv.get("/redirect") { redirect("http://example.com") }

        srv.after("/hi") {
            response.addHeader ("after", "foobar")
        }

        srv.get("/fortunes") {
            page {
                html {
                    head {
                        title { +"Fortunes" }
                    }
                    body {
                        table {
                            tr {
                                th { +"id" }
                                th { +"message" }
                            }
                            FORTUNE_MESSAGES.forEachIndexed { index, fortune ->
                                tr {
                                    td { +index }
                                    td { +fortune }
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    private fun Exchange.okRequestMethod() = ok (request.method)

    fun reqres() {
        withClients {
            val response = get("/reqres")
            assertResponseEquals(response, 200, "GET")
        }
    }

    fun getHi() {
        withClients {
            val response = get("/hi")
            assertResponseEquals(response, 200, "Hello World!")
        }
    }

    fun hiHead() {
        withClients {
            val response = head("/hi")
            assertResponseEquals(response, 200, "")
        }
    }

    fun template() {
        withClients {
            val response = get("/template")
            assert(response.statusCode == 200)
        }
    }

    fun getHiAfterFilter() {
        withClients {
            val response = get ("/hi")
            assertResponseEquals(response, 200, "Hello World!")
            assert(response.headers["after"]?.contains("foobar") ?: false)
        }
    }

    fun getRoot() {
        withClients {
            val response = get ("/")
            assertResponseEquals(response, 200, "Hello Root!")
        }
    }

    fun echoParam1() {
        withClients {
            val response = get ("/param/shizzy")
            assertResponseEquals(response, 200, "echo: shizzy")
        }
    }

    fun echoParam2() {
        withClients {
            val response = get ("/param/gunit")
            assertResponseEquals(response, 200, "echo: gunit")
        }
    }

    fun echoParamWithUpperCaseInValue() {
        withClients {
            val camelCased = "ThisIsAValueAndBlacksheepShouldRetainItsUpperCasedCharacters"
            val response = get ("/param/" + camelCased)
            assertResponseEquals(response, 200, "echo: $camelCased")
        }
    }

    fun twoRoutesWithDifferentCase() {
        withClients {
            var expected = "expected"
            val response1 = get ("/tworoutes/$part/$expected")
            assertResponseEquals(response1, 200, "$part route: $expected")

            expected = expected.toUpperCase()
            val response = get ("/tworoutes/${part.toUpperCase()}/$expected")
            assertResponseEquals(response, 200, "${part.toUpperCase()} route: $expected")
        }
    }

    fun echoParamWithMaj() {
        withClients {
            val response = get ("/paramwithmaj/plop")
            assertResponseEquals(response, 200, "echo: plop")
        }
    }

    fun unauthorized() {
        withClients {
            val response = get ("/protected/resource")
            assertTrue(response.statusCode == 401)
        }
    }

    fun notFound() {
        withClients {
            val response = get ("/no/resource")
            assertResponseContains(response, 404, "http://localhost:", "/no/resource not found")
        }
    }

    fun postOk() {
        withClients {
            val response = post ("/poster", "Fo shizzy")
            assertResponseContains(response, 201, "Fo shizzy")
        }
    }

    fun patchOk() {
        withClients {
            val response = patch ("/patcher", "Fo shizzy")
            assertResponseContains(response, 200, "Fo shizzy")
        }
    }

    fun staticFile() {
        withClients {
            val response = get ("/file.txt")
            assertResponseEquals(response, 200, "file content\n")
        }
    }

    fun fileContentType() {
        withClients {
            val response = get ("/file.css")
            assert(response.contentType.contains("css"))
            assertResponseEquals(response, 200, "/* css */\n")
        }
    }

    fun halt() {
        withClients {
            val response = get ("/halt")
            assertResponseEquals(response, 500, "halted")
        }
    }

    fun redirect() {
        withClients {
            val response = get ("/redirect")
            assert(response.statusCode == 302)
            assert(response.headers["Location"] == "http://example.com")
        }
    }

    // TODO Check with asserts
    fun requestData() {
        withClients {
            val response = get ("/request/data?query")
            val port = endpointUrl.port.toString ()
            val protocol = "http"
//            val protocol = if (testScenario.secure) "https" else "http"

//            assert ("error message" == response.cookies["method"].value)
//            assert ("error message" == response.cookies["host"].value)
//            assert ("error message" == response.cookies["uri"].value)
//            assert ("error message" == response.cookies["params"].value)

            assert("AHC/2.0" == response.headers["agent"])
            assert(protocol == response.headers["scheme"])
            assert("127.0.0.1" == response.headers["host"])
            assert("query" == response.headers["query"])
            assert(port == response.headers["port"])

            assert(response.responseBody == "$protocol://localhost:$port/request/data!!!")
            assert(200 == response.statusCode)
        }
    }

    fun handleException() {
        withClients {
            val response = get ("/exception")
            assert("error message" == response.headers["error"]?.toString())
        }
    }

    fun methods () {
        withClients {
            checkMethod (this, "HEAD", "header") // Head does not support body message
            checkMethod (this, "DELETE")
            checkMethod (this, "OPTIONS")
            checkMethod (this, "GET")
            checkMethod (this, "PATCH")
            checkMethod (this, "POST")
            checkMethod (this, "PUT")
            checkMethod (this, "TRACE")
        }
    }

    private fun checkMethod (client: Client, methodName: String, headerName: String? = null) {
        val res = client.send(HttpMethod.valueOf (methodName), "/method")
        assert (
            if (headerName == null) res.responseBody != null
            else res.headers.get(headerName) == methodName
        )
        assert (200 == res.statusCode)
    }

    fun fortunes() {
        withClients {
            val response = get("/fortunes")
            val content = response.responseBody

            assert(response.headers ["Date"] != null)
            assert(response.headers ["Server"] != null)
            assert(response.headers ["Transfer-Encoding"] != null)
            assert(response.headers ["Content-Type"] == "text/html;charset=utf-8")

            assert(content.contains("<td>&lt;script&gt;alert(&quot;This should not be "))
            assert(content.contains(" displayed in a browser alert box.&quot;);&lt;/script&gt;</td>"))
            assert(content.contains("<td>フレームワークのベンチマーク</td>"))
        }
    }
}

