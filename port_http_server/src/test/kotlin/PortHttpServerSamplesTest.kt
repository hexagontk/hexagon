package com.hexagonkt.http.server

import com.hexagonkt.helpers.CodedException
import com.hexagonkt.http.Method.POST
import com.hexagonkt.http.Part
import com.hexagonkt.http.Path
import com.hexagonkt.http.client.Client
import com.hexagonkt.http.client.Request
import com.hexagonkt.http.client.ahc.AhcAdapter
import com.hexagonkt.injection.InjectionManager
import com.hexagonkt.serialization.Json
import org.junit.jupiter.api.Test
import java.lang.IllegalStateException
import java.net.HttpCookie
import java.net.InetAddress
import java.net.URL

import com.hexagonkt.http.client.Response as ClientResponse

abstract class PortHttpServerSamplesTest(val adapter: ServerPort) {

    private data class Type(val value: String = "value")

    @Test fun serverCreation() {
        // serverCreation
        /*
         * All settings are optional, you can supply any combination
         * Parameters not set will fall back to the defaults
         */
        val settings = ServerSettings(
            serverName = "name",
            bindAddress = InetAddress.getByName("0.0.0"),
            bindPort = 2020,
            contextPath = "/context"
        )

        val router = Router {
            get("/hello") { ok("Hello World!") }
        }

        val customServer = Server(adapter, router, settings)

        customServer.start()

        val customClient = Client(AhcAdapter(), "http://localhost:${customServer.runtimePort}")
        assert(customServer.started())
        assert(customClient.get("/context/hello").body == "Hello World!")

        customServer.stop()

        /*
         * You can skip the adapter is you previously bound one
         * You may also skip the settings an the defaults will be used
         */
        InjectionManager.bindObject(adapter)
        val defaultServer = Server(router = router)

        defaultServer.start()

        val defaultClient = Client(AhcAdapter(), "http://localhost:${defaultServer.runtimePort}")
        assert(defaultServer.started())
        assert(defaultClient.get("/hello").body == "Hello World!")

        defaultServer.stop()
        // serverCreation
    }

    @Test fun routesCreation() {
        val server = Server(adapter) {
            // routesCreation
            get("/hello") { ok("Get greeting")}
            put("/hello") { ok("Put greeting")}
            post("/hello") { ok("Post greeting")}

            any("/hello") { ok("Fallback if HTTP verb was not used before")}

            get { ok("Get at '/' if no route matched before") }
            // routesCreation
        }

        server.start()
        val client = Client(AhcAdapter(), "http://localhost:${server.runtimePort}")
        assert(client.get("/hello").body == "Get greeting")
        assert(client.put("/hello").body == "Put greeting")
        assert(client.post("/hello").body == "Post greeting")
        assert(client.options("/hello").body == "Fallback if HTTP verb was not used before")
        assert(client.get("/").body == "Get at '/' if no route matched before")
        server.stop()
    }

    @Test fun routeGroups() {
        val server = Server(adapter) {
            // routeGroups
            path("/nested") {
                get("/hello") { ok("Greeting")}

                path("/secondLevel") {
                    get("/hello") { ok("Second level greeting")}
                }

                get { ok("Get at '/nested'") }
            }
            // routeGroups
        }

        server.start()
        val client = Client(AhcAdapter(), "http://localhost:${server.runtimePort}")
        assert(client.get("/nested/hello").body == "Greeting")
        assert(client.get("/nested/secondLevel/hello").body == "Second level greeting")
        assert(client.get("/nested").body == "Get at '/nested'")
        server.stop()
    }

    @Test fun routers() {
        // routers
        fun personRouter(kind: String) = Router {
            get { ok("Get $kind") }
            put { ok("Put $kind") }
            post { ok("Post $kind") }
        }

        val server = Server(adapter) {
            path("/clients", personRouter("client"))
            path("/customers", personRouter("customer"))
        }
        // routers

        server.start()
        val client = Client(AhcAdapter(), "http://localhost:${server.runtimePort}")

        assert(client.get("/clients").body == "Get client")
        assert(client.put("/clients").body == "Put client")
        assert(client.post("/clients").body == "Post client")

        assert(client.get("/customers").body == "Get customer")
        assert(client.put("/customers").body == "Put customer")
        assert(client.post("/customers").body == "Post customer")

        server.stop()
    }

    @Suppress("UNREACHABLE_CODE")
    @Test fun callbacks() {
        val server = Server(adapter) {
            // callbackCall
            get("/call") {
                attributes                   // the attributes list
                attributes["foo"]            // value of foo attribute
                attributes["A"] = "V"        // sets value of attribute A to V

                ok("Response body")          // returns a 200 status
                send(400, "Invalid request") // returns any status
            }
            // callbackCall

            // callbackRequest
            get("/request") {
                // URL Information
                request.method                   // the HTTP method (GET, ..etc)
                request.scheme                   // http or https
                request.secure                   // true if scheme is https
                request.host                     // the host, e.g. "example.com"
                request.ip                       // client IP address
                request.port                     // the server port
                request.path                     // the request path, e.g. /result.jsp
                request.body                     // request body sent by the client
                request.url                      // the url. e.g. "http://example.com/foo"

                // Headers
                request.headers                  // the HTTP header list with first values only
                request.headers["BAR"]           // first value of BAR header
                request.headersValues            // the HTTP header list with their full values list
                request.headersValues["BAR"]     // list of values of BAR header

                // Common headers shortcuts
                request.contentType              // content type of request.body
                request.acceptValues             // Client accepted content types
                request.userAgent                // user agent (browser requests)
                request.origin                   // origin (browser requests)

                // Parameters
                request.pathParameters           // map with all path parameters
                request.formParameters           // map with first values of all form fields
                request.formParametersValues     // map with all form fields values
                request.queryParameters          // map with first values of all query parameters
                request.queryParametersValues    // map with all query parameters values

                // Body processing
                request.contentLength            // length of request body
                request.body(Type::class)        // Object passed in the body as a typed object
                request.body<Type>()             // Syntactic sugar for the previous statement
                request.bodyObjects(Type::class) // Object(s) passed in the body as a typed list
                request.bodyObjects<Type>()      // Syntactic sugar for the previous statement
                request.body(Map::class)         // Object passed in the body as a field map
                request.body<Map<*, *>>()        // Syntactic sugar for the previous statement
                request.bodyObjects(Map::class)  // Object(s) passed in the body as a list of maps
                request.bodyObjects<Map<*, *>>() // Syntactic sugar for the previous statement
            }
            // callbackRequest

            // callbackResponse
            get("/response") {
                response.body                             // get response content
                response.body = "Hello"                   // sets content to Hello
                response.headers["FOO"] = "bar"           // sets header FOO with single value bar
                response.headersValues["FOO"] = listOf("bar") // sets header FOO values with [ bar ]
                response.status                           // get the response status
                response.status = 401                     // set status code to 401
                response.contentType                      // get the content type
                response.contentType = "text/xml"         // set content type to text/xml
            }
            // callbackResponse

            // callbackPathParam
            get("/pathParam/{foo}") {
                request.pathParameters["foo"] // value of foo path parameter
                request.pathParameters        // map with all parameters
            }
            // callbackPathParam

            // callbackQueryParam
            get("/queryParam") {
                request.queryString
                request.queryParameters                       // the query param list
                request.queryParameters["FOO"]                // value of FOO query param
                request.queryParametersValues                 // the query param list
                request.queryParametersValues["FOO"]          // all values of FOO query param
            }
            // callbackQueryParam

            // callbackFormParam
            get("/formParam") {
                request.formParameters                       // the query param list
                request.formParameters["FOO"]                // value of FOO query param
                request.formParametersValues                 // the query param list
                request.formParametersValues["FOO"]          // all values of FOO query param
            }
            // callbackFormParam

            // callbackFile
            post("/file") {
                val filePart = request.parts["file"] ?: error("File not available")
                ok(filePart.inputStream.reader().readText())
            }
            // callbackFile

            // callbackRedirect
            get("/redirect") {
                redirect("/call") // browser redirect to /call
            }
            // callbackRedirect

            // callbackCookie
            get("/cookie") {
                request.cookies                       // get map of all request cookies
                request.cookies["foo"]                // access request cookie by name

                val cookie = HttpCookie("new_foo", "bar")
                response.addCookie(cookie)            // set cookie with a value

                cookie.maxAge = 3600
                response.addCookie(cookie)            // set cookie with a max-age

                cookie.secure = true
                response.addCookie(cookie)            // secure cookie

                response.removeCookie("foo")          // remove cookie
            }
            // callbackCookie

            // callbackSession
            get("/session") {
                session                         // create and return session
                session.attributes["user"]      // Get session attribute 'user'
                session.set("user", "foo")      // Set session attribute 'user'
                session.removeAttribute("user") // Remove session attribute 'user'
                session.attributes              // Get all session attributes
                session.id                      // Get session id
                session.isNew()                 // Check if session is new
            }
            // callbackSession

            // callbackHalt
            get("/halt") {
                halt()                // halt with status 500 and stop route processing

                /*
                 * These are just examples the following code will never be reached
                 */
                halt(401)             // halt with status
                halt("Body Message")  // halt with message (status 500)
                halt(401, "Go away!") // halt with status and message
            }
            // callbackHalt
        }

        server.start()
        val client = Client(AhcAdapter(), "http://localhost:${server.runtimePort}")
        client.cookies["foo"] = HttpCookie("foo", "bar")

        val callResponse = client.get("/call")
        assert(callResponse.status == 400)
        assert(callResponse.body == "Invalid request")
        assert(client.get("/request", body = Type(), contentType = Json.contentType).status == 200)
        assert(client.get("/response").status == 401)
        assert(client.get("/pathParam/param").status == 200)
        assert(client.get("/queryParam").status == 200)
        assert(client.get("/formParam").status == 200)
        assert(client.get("/redirect").status == 302)
        assert(client.get("/cookie").status == 200)
        assert(client.get("/session").status == 200)
        assert(client.get("/halt").status == 500)

        val stream = URL("classpath:assets/index.html").openStream()
        val parts = mapOf("file" to Part("file", stream, "index.html"))
        val response = client.send(Request(POST, Path("/file"), parts = parts))
        assert(response.body?.contains("<title>Hexagon</title>") ?: false)

        server.stop()
    }

    @Test fun filters() {
        fun assertResponse(response: ClientResponse, body: String, vararg headers: String) {
            assert(response.status == 200)
            (headers.toList() + "b_all" + "a_all").forEach { assert(response.headers.contains(it)) }
            assert(response.body == body)
        }

        fun assertFail(code: Int, response: ClientResponse, body: String, vararg headers: String) {
            assert(response.status == code)
            (headers.toList() + "b_all" + "a_all").forEach { assert(response.headers.contains(it)) }
            assert(response.body == body)
        }

        val server = Server(adapter) {
            // filters
            before { response.headersValues["b_all"] = listOf("true") }

            before("/filters/*") { response.headersValues["b_filters"] = listOf("true") }
            get("/filters/route") { ok("filters route") }
            after("/filters/*") { response.headersValues["a_filters"] = listOf("true") }

            get("/filters") { ok("filters") }

            path("/nested") {
                before { response.headersValues["b_nested"] = listOf("true") }
                before("/") { response.headersValues["b_nested_2"] = listOf("true") }
                get("/filters") { ok("nested filters") }
                get("/halted") { halt(499, "halted") }
                get { ok("nested also") }
                after { response.headersValues["a_nested"] = listOf("true") }
            }

            after { response.headersValues["a_all"] = listOf("true") }
            // filters
        }

        server.start()
        val client = Client(AhcAdapter(), "http://localhost:${server.runtimePort}")

        assertResponse(client.get("/filters/route"), "filters route", "b_filters", "a_filters")
        assertResponse(client.get("/filters"), "filters")
        assertResponse(client.get("/nested/filters"), "nested filters", "b_nested", "a_nested")
        assertResponse(client.get("/nested"), "nested also", "b_nested", "b_nested_2", "a_nested")
        assertFail(499, client.get("/nested/halted"), "halted", "b_nested", "a_nested")
        assert(!client.get("/filters/route").headers.contains("b_nested"))
        assert(!client.get("/filters/route").headers.contains("a_nested"))

        server.stop()
    }

    @Test fun errors() {
        val server = Server(adapter) {
            // errors
            // Register handler for routes halted with 512 code
            error(512) { send(500, "Ouch")}

            // If status code (512) is returned with `send` error won't be triggered
            get("/errors") { halt(512) }
            // errors

            // exceptions
            // Register handler for routes which callbacks throw exceptions
            error(CodedException::class) { send(599, it.message ?: "empty") }
            error(IllegalStateException::class) { send(505, it.message ?: "empty") }
            get("/exceptions") { error("Message") }
            get("/codedExceptions") { halt(509, "code") }
            // exceptions
        }

        server.start()
        val client = Client(AhcAdapter(), "http://localhost:${server.runtimePort}")

        val errors = client.get("/errors")
        assert(errors.status == 500)
        assert(errors.body == "Ouch")
        val exceptions = client.get("/exceptions")
        assert(exceptions.status == 505)
        assert(exceptions.body == "Message")
        val codedExceptions = client.get("/codedExceptions")
        assert(codedExceptions.status == 599)
        assert(codedExceptions.body == "code")

        server.stop()
    }

    @Test fun files() {
        val server = Server(adapter) {
            // files
            get("/web/file.txt") { ok("It matches this route and won't search for the file") }

            // Expose resources on the '/public' resource folder over the '/web' HTTP path
            get("/web/*", URL("classpath:public"))

            // Maps resources on 'assets' on the server root (assets/f.css -> /f.css)
            // '/public/css/style.css' resource would be: 'http://{host}:{port}/css/style.css'
            get(URL("classpath:assets"))
            // files
        }

        server.start()
        val client = Client(AhcAdapter(), "http://localhost:${server.runtimePort}")

        assert(client.get("/web/file.txt").body?.startsWith("It matches this route") ?: false)

        val index = client.get("/index.html")
        assert(index.status == 200)
        assert(index.contentType == "text/html")
        val file = client.get("/web/file.css")
        assert(file.status == 200)
        assert(file.contentType == "text/css")

        val unavailable = client.get("/web/unavailable.css")
        assert(unavailable.status == 404)

        server.stop()
    }

    @Test fun test() {
        // test
        val router = Router {
            get("/hello") { ok("Hi!") }
        }

        val serverSettings = ServerSettings("name", InetAddress.getLoopbackAddress(), 0)
        val server = Server(adapter, router, serverSettings)

        server.start()
        val client = Client(AhcAdapter(), "http://localhost:${server.runtimePort}")
        assert(client.get("/hello").body == "Hi!")
        server.stop()
        // test
    }
}
