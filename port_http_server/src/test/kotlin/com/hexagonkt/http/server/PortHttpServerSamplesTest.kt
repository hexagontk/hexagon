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
            // callbackCall

            // callbackRequest
            // callbackRequest

            // callbackResponse
            // callbackResponse

            // callbackQueryParam
            // callbackQueryParam

            // callbackPathParam
            // callbackPathParam

            // callbackRedirect
            // callbackRedirect

            // callbackCookie
            // callbackCookie

            // callbackSession
            // callbackSession

            // callbackHalt
            // callbackHalt
        }

        server.start()
        val client = Client("http://localhost:${server.runtimePort}")

        server.stop()
    }
}
