package com.hexagonkt.http.server.examples

import com.hexagonkt.http.client.Client
import com.hexagonkt.helpers.Logger
import com.hexagonkt.http.server.Router
import com.hexagonkt.http.server.Server
import com.hexagonkt.http.server.ServerPort
import org.asynchttpclient.Response
import org.testng.annotations.AfterClass
import org.testng.annotations.BeforeClass
import org.testng.annotations.Test

@Test abstract  class SessionTest(adapter: ServerPort) {
    private val log: Logger = Logger(this)

    private val server: Server by lazy {
        Server(adapter) {
            get("/session/id") {
                val id: String = session.id ?: "null"
                assert(id == session.id ?: "null")
                ok(id)
            }

            get("/session/access") { ok(session.lastAccessedTime?.toString() ?: "null") }

            get("/session/new") {
                try {
                    ok(session.isNew())
                }
                catch (e: Exception) {
                    halt("Session error")
                }
            }

            get("/session/inactive") {
                val inactiveInterval = session.maxInactiveInterval ?: "null"
                session.maxInactiveInterval = 999
                assert(inactiveInterval == session.maxInactiveInterval ?: "null")
                ok(inactiveInterval)
            }

            get("/session/creation") { ok(session.creationTime ?: "null") }

            post("/session/invalidate") { session.invalidate() }

            put("/session/{key}/{value}") {
                session.setAttribute(pathParameters["key"], pathParameters["value"])
            }

            get("/session/{key}") {
                ok(session.getAttribute(pathParameters["key"]).toString())
            }

            delete("/session/{key}") {
                session.removeAttribute(pathParameters["key"])
            }

            get("/session") {
                val attributeTexts = session.attributes.entries.map { it.key + " : " + it.value }

                response.setHeader("attributes", attributeTexts.joinToString(", "))
                response.setHeader("attribute values", session.attributes.values.joinToString(", "))
                response.setHeader("attribute names", session.attributes.keys.joinToString(", "))

                response.setHeader("creation", session.creationTime.toString())
                response.setHeader("id", session.id ?: "")
                response.setHeader("last access", session.lastAccessedTime.toString())

                response.status = 200
            }
        }
    }

    private val client: Client by lazy { Client("http://localhost:${server.runtimePort}") }

    @BeforeClass fun initialize() {
        server.start()
    }

    @AfterClass fun shutdown() {
        server.stop()
    }

    @Test fun attribute() {
        assert(client.put("/session/foo/bar").statusCode == 200)
        assertResponseEquals(client.get("/session/foo"), "bar")
    }

    @Test fun sessionLifecycle() {
        client.post("/session/invalidate")

        assert(client.get("/session/id").responseBody == "null")
        assert(client.get("/session/inactive").responseBody == "null")
        assert(client.get("/session/creation").responseBody == "null")
        assert(client.get("/session/access").responseBody == "null")
        assert(client.get("/session/new").responseBody == "true")

        assert(client.put("/session/foo/bar").statusCode == 200)
        assert(client.put("/session/foo/bazz").statusCode == 200)
        assert(client.put("/session/temporal/_").statusCode == 200)
        assert(client.delete("/session/temporal").statusCode == 200)

        assert(client.get("/session").statusCode == 200)
        assertResponseEquals(client.get("/session/foo"), "bazz")

        assert(client.get("/session/id").responseBody != "null")
        assert(client.get("/session/inactive").responseBody != "null")
        assert(client.get("/session/creation").responseBody != "null")
        assert(client.get("/session/access").responseBody != "null")
        assert(client.get("/session/new").responseBody == "false")

        client.post("/session/invalidate")

        assert(client.get("/session/id").responseBody == "null")
        assert(client.get("/session/inactive").responseBody == "null")
        assert(client.get("/session/creation").responseBody == "null")
        assert(client.get("/session/access").responseBody == "null")
    }

    protected fun assertResponseEquals(response: Response?, content: String, status: Int = 200) {
        assert (response?.statusCode == status)
        assert (response?.responseBody == content)
    }

    protected fun assertResponseContains(response: Response?, status: Int, vararg content: String) {
        assert (response?.statusCode == status)
        content.forEach {
            assert (response?.responseBody?.contains (it) ?: false)
        }
    }

    protected fun assertResponseContains(response: Response?, vararg content: String) {
        assertResponseContains(response, 200, *content)
    }
}
