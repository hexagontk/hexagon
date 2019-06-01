package com.hexagonkt.http.server

import com.hexagonkt.http.client.Client
import com.hexagonkt.injection.InjectionManager
import org.testng.annotations.Test
import java.net.InetAddress

@Test abstract class PortHttpServerSamplesTest(val adapter: ServerPort) {

    @Test fun serverCreation() {
        // serverCreation
        val customServer = Server(adapter, Router(), "name", InetAddress.getByName("0.0.0"), 2020)

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
            }
            // callbackCookie

            // callbackSession
            get("/session") {
                session                // session management
            }
            // callbackSession

            // callbackHalt
            get("/halt") {
                request.cookies                // request cookies sent by the client
            }
            // callbackHalt
        }

        server.start()
        val client = Client("http://localhost:${server.runtimePort}")

        val callResponse = client.get("/call")
        assert(callResponse.statusCode == 400)
        assert(callResponse.responseBody == "Invalid request")
        assert(client.get("/request").statusCode == 200)
        assert(client.get("/response").statusCode == 401)
        assert(client.get("/pathParam/param").statusCode == 200)
        assert(client.get("/queryParam").statusCode == 200)
        assert(client.get("/redirect").statusCode == 302)

        server.stop()
    }
}
