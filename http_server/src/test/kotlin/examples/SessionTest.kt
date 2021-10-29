package com.hexagonkt.http.server.examples

import com.hexagonkt.core.helpers.require
import com.hexagonkt.http.client.Client
import com.hexagonkt.http.client.ahc.AhcAdapter
import com.hexagonkt.http.server.Server
import com.hexagonkt.http.server.ServerPort
import com.hexagonkt.http.client.Response
import com.hexagonkt.http.server.ServerFeature.SESSIONS
import com.hexagonkt.http.server.ServerSettings
import org.junit.jupiter.api.AfterAll
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestInstance
import org.junit.jupiter.api.TestInstance.Lifecycle.PER_CLASS

@TestInstance(PER_CLASS)
abstract class SessionTest(adapter: ServerPort) {

    // session
    val server: Server = Server(adapter, ServerSettings(features = setOf(SESSIONS))) {
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

                response.headers["attributes"] = attributeTexts.joinToString(", ")
                response.headers["attribute values"] = attributes.values.joinToString(", ")
                response.headers["attribute names"] = attributes.keys.joinToString(", ")

                response.headers["creation"] = session.creationTime.toString()
                response.headers["id"] = session.id ?: ""
                response.headers["last access"] = session.lastAccessedTime.toString()

                response.status = 200
            }
        }
    }
    // session

    private val client: Client by lazy {
        Client(AhcAdapter(), "http://localhost:${server.runtimePort}")
    }

    @BeforeAll fun initialize() {
        server.start()
    }

    @AfterAll fun shutdown() {
        server.stop()
    }

    @Test fun `Attribute is added to session`() {
        assert(client.put("/session/foo/bar").status == 200)
        assertResponseEquals(client.get("/session/foo"), "bar")
    }

    @Test fun `Session attribute lifecycle test`() {
        client.post("/session/invalidate")

        assert(client.get("/session/id").body == "null")
        assert(client.get("/session/inactive").body == "null")
        assert(client.get("/session/creation").body == "null")
        assert(client.get("/session/access").body == "null")
        assert(client.get("/session/new").body == "true")

        assert(client.put("/session/foo/bar").status == 200)
        assert(client.put("/session/foo/bazz").status == 200)
        assert(client.put("/session/temporal/_").status == 200)
        assert(client.delete("/session/temporal").status == 200)

        assert(client.get("/session").status == 200)
        assertResponseEquals(client.get("/session/foo"), "bazz")

        assert(client.get("/session/id").body != "null")
        assert(client.get("/session/inactive").body != "null")
        assert(client.get("/session/creation").body != "null")
        assert(client.get("/session/access").body != "null")
        assert(client.get("/session/new").body == "false")

        assert(client.put("/session/inactive/10").status == 200)
        assert(client.get("/session/inactive").body == "10")

        client.post("/session/invalidate")

        assert(client.get("/session/id").body == "null")
        assert(client.get("/session/inactive").body == "null")
        assert(client.get("/session/creation").body == "null")
        assert(client.get("/session/access").body == "null")
    }

    private fun assertResponseEquals(response: Response?, content: String, status: Int = 200) {
        assert (response?.status == status)
        assert (response?.body == content)
    }
}
