package com.hexagonkt.http.server

import com.hexagonkt.http.client.Client
import com.hexagonkt.injection.InjectionManager
import io.netty.handler.codec.http.cookie.DefaultCookie
import org.testng.annotations.Test
import java.lang.IllegalStateException
import java.net.HttpCookie
import java.net.InetAddress

import org.asynchttpclient.Response as ClientResponse

@Test abstract class PortHttpServerSamplesTest(val adapter: ServerPort) {

    @Test fun serverCreation() {
        // serverCreation
        val serverSettings = ServerSettings("name", InetAddress.getByName("0.0.0"), 2020)
        val customServer = Server(adapter, Router(), serverSettings)

        customServer.start()
        assert(customServer.started())
        customServer.stop()

        InjectionManager.bindObject(adapter)
        val defaultServer = Server {}

        defaultServer.start()
        assert(defaultServer.started())
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
        val client = Client("http://localhost:${server.runtimePort}")
        assert(client.get("/hello").responseBody == "Get greeting")
        assert(client.put("/hello").responseBody == "Put greeting")
        assert(client.post("/hello").responseBody == "Post greeting")
        assert(client.options("/hello").responseBody == "Fallback if HTTP verb was not used before")
        assert(client.get("/").responseBody == "Get at '/' if no route matched before")
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
        val client = Client("http://localhost:${server.runtimePort}")
        assert(client.get("/nested/hello").responseBody == "Greeting")
        assert(client.get("/nested/secondLevel/hello").responseBody == "Second level greeting")
        assert(client.get("/nested").responseBody == "Get at '/nested'")
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
        val client = Client("http://localhost:${server.runtimePort}")

        assert(client.get("/clients").responseBody == "Get client")
        assert(client.put("/clients").responseBody == "Put client")
        assert(client.post("/clients").responseBody == "Post client")

        assert(client.get("/customers").responseBody == "Get customer")
        assert(client.put("/customers").responseBody == "Put customer")
        assert(client.post("/customers").responseBody == "Post customer")

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
                request.method         // the HTTP method (GET, ..etc)
                request.scheme         // http or https
                request.secure         // true if scheme is https
                request.host           // the host, e.g. "example.com"
                request.ip             // client IP address
                request.port           // the server port
                request.path           // the request path, e.g. /result.jsp
                request.body           // request body sent by the client
                request.url            // the url. e.g. "http://example.com/foo"
                request.contentLength  // length of request body
                request.contentType    // content type of request.body
                request.headers        // the HTTP header list
                request.headers["BAR"] // value of BAR header
                request.userAgent      // user agent
            }
            // callbackRequest

            // callbackResponse
            get("/response") {
                response.body                           // get response content
                response.body = "Hello"                 // sets content to Hello
                response.headers["FOO"] = listOf("bar") // sets header FOO with value bar
                response.status                         // get the response status
                response.status = 401                   // set status code to 401
                response.contentType                    // get the content type
                response.contentType = "text/xml"       // set content type to text/xml
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
                request.parameters                 // the query param list
                request.parameters["FOO"]?.first() // value of FOO query param
                request.parameters["FOO"]          // all values of FOO query param
            }
            // callbackQueryParam

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
        val client = Client("http://localhost:${server.runtimePort}")
        client.cookies["foo"] = DefaultCookie("foo", "bar")

        val callResponse = client.get("/call")
        assert(callResponse.statusCode == 400)
        assert(callResponse.responseBody == "Invalid request")
        assert(client.get("/request").statusCode == 200)
        assert(client.get("/response").statusCode == 401)
        assert(client.get("/pathParam/param").statusCode == 200)
        assert(client.get("/queryParam").statusCode == 200)
        assert(client.get("/redirect").statusCode == 302)
        assert(client.get("/cookie").statusCode == 200)
        assert(client.get("/session").statusCode == 200)
        assert(client.get("/halt").statusCode == 500)

        server.stop()
    }

    @Test fun filters() {
        fun assertResponse(response: ClientResponse, body: String, vararg headers: String) {
            assert(response.statusCode == 200)
            (headers.toList() + "b_all" + "a_all").forEach { assert(response.headers.contains(it)) }
            assert(response.responseBody == body)
        }

        val server = Server(adapter) {
            // filters
            before { response.headers["b_all"] = listOf("true") }

            before("/filters/*") { response.headers["b_filters"] = listOf("true") }
            get("/filters/route") { ok("filters route") }
            after("/filters/*") { response.headers["a_filters"] = listOf("true") }

            get("/filters") { ok("filters") }

            path("/nested") {
                before { response.headers["b_nested"] = listOf("true") }
                get("/filters") { ok("nested filters") }
                after { response.headers["a_nested"] = listOf("true") }
            }

            after { response.headers["a_all"] = listOf("true") }
            // filters
        }

        server.start()
        val client = Client("http://localhost:${server.runtimePort}")

        assertResponse(client.get("/filters/route"), "filters route", "b_filters", "a_filters")
        assertResponse(client.get("/filters"), "filters")
        assertResponse(client.get("/nested/filters"), "nested filters", "b_nested", "a_nested")

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
            // Register handler for routes which callbacks throw an `IllegalStateException`
            error(IllegalStateException::class) { send(505, it.message ?: "empty") }
            get("/exceptions") { error("Message") }
            // exceptions
        }

        server.start()
        val client = Client("http://localhost:${server.runtimePort}")

        val errors = client.get("/errors")
        assert(errors.statusCode == 500)
        assert(errors.responseBody == "Ouch")
        val exceptions = client.get("/exceptions")
        assert(exceptions.statusCode == 505)
        assert(exceptions.responseBody == "Message")

        server.stop()
    }

    @Test fun files() {
        val server = Server(adapter) {
            // files
            get("/web/file.txt") { ok("It matches this route and won't search for the file") }

            // Expose resources on the '/public' resource folder over the '/web' HTTP path
            assets("public", "/web/*")

            // Maps resources on 'assets' on the server root (assets/f.css -> /f.css)
            // '/public/css/style.css' resource would be: 'http://{host}:{port}/css/style.css'
            assets("assets")
            // files
        }

        server.start()
        val client = Client("http://localhost:${server.runtimePort}")

        assert(client.get("/web/file.txt").responseBody.startsWith("It matches this route"))

        val index = client.get("/index.html")
        assert(index.statusCode == 200)
        assert(index.contentType == "text/html")
        val file = client.get("/web/file.css")
        assert(file.statusCode == 200)
        assert(file.contentType == "text/css")

        val unavailable = client.get("/web/unavailable.css")
        assert(unavailable.statusCode == 404)

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
        val client = Client("http://localhost:${server.runtimePort}")
        assert(client.get("/hello").responseBody == "Hi!")
        server.stop()
        // test
    }
}
