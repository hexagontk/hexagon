package com.hexagonkt.http.server.examples

import com.hexagonkt.helpers.require
import com.hexagonkt.http.client.Client
import com.hexagonkt.http.server.Server
import com.hexagonkt.http.server.ServerPort
import org.asynchttpclient.Response
import org.testng.annotations.AfterClass
import org.testng.annotations.BeforeClass
import org.testng.annotations.Test

@Test abstract class SessionTest(adapter: ServerPort) {

    // session
    val server: Server by lazy {
        Server(adapter) {
            path("/session") {
                get("/id") { ok(session.id ?: "null") }
                get("/access") { ok(session.lastAccessedTime?.toString() ?: "null") }
                get("/new") { ok(session.isNew()) }

                path("/inactive") {
                    get { ok(session.maxInactiveInterval ?: "null") }

                    put("/{time}") {
                        session.maxInactiveInterval = pathParameters.require("time").toInt()
                    }
                }

                get("/creation") { ok(session.creationTime ?: "null") }
                post("/invalidate") { session.invalidate() }

                path("/{key}") {
                    put("/{value}") {
                        session.set(pathParameters.require("key"), pathParameters.require("value"))
                    }

                    get { ok(session.get(pathParameters.require("key")).toString()) }
                    delete { session.remove(pathParameters.require("key")) }
                }

                get {
                    val attributes = session.attributes
                    val attributeTexts = attributes.entries.map { it.key + " : " + it.value }

                    response.setHeader("attributes", attributeTexts.joinToString(", "))
                    response.setHeader("attribute values", attributes.values.joinToString(", "))
                    response.setHeader("attribute names", attributes.keys.joinToString(", "))

                    response.setHeader("creation", session.creationTime.toString())
                    response.setHeader("id", session.id ?: "")
                    response.setHeader("last access", session.lastAccessedTime.toString())

                    response.status = 200
                }
            }
        }
    }
    // session

    private val client: Client by lazy { Client("http://localhost:${server.runtimePort}") }

    @BeforeClass fun initialize() {
        server.start()
    }

    @AfterClass fun shutdown() {
        server.stop()
    }

    @Test fun `Attribute is added to session`() {
        assert(client.put("/session/foo/bar").statusCode == 200)
        assertResponseEquals(client.get("/session/foo"), "bar")
    }

    @Test fun `Session attribute lifecycle test`() {
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

        assert(client.put("/session/inactive/10").statusCode == 200)
        assert(client.get("/session/inactive").responseBody == "10")

        client.post("/session/invalidate")

        assert(client.get("/session/id").responseBody == "null")
        assert(client.get("/session/inactive").responseBody == "null")
        assert(client.get("/session/creation").responseBody == "null")
        assert(client.get("/session/access").responseBody == "null")
    }

    private fun assertResponseEquals(response: Response?, content: String, status: Int = 200) {
        assert (response?.statusCode == status)
        assert (response?.responseBody == content)
    }
}
