package co.there4.hexagon.server.integration

import co.there4.hexagon.server.*

@Suppress("unused") // Test methods are flagged as unused
class SessionIT(serverEngine: ServerEngine) : ItModule(serverEngine) {
    override fun Router.initialize() {
        get("/session/id") {
            val id: String = session.id ?: "null"
            session.id = "sessionId"
            assert(id == session.id ?: "null")
            ok(id)
        }

        get("/session/access") { ok(session.lastAccessedTime?.toString() ?: "null") }

        get("/session/new") {
            try {
                ok(session.isNew())
            }
            catch(e: Exception) {
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
            session [request.parameter("key")] = request.parameter("value")
            Unit
        }

        get("/session/{key}") {
            ok (session [request.parameter("key")].toString())
        }

        delete("/session/{key}") {
            session.removeAttribute(request.parameter("key"))
        }

        get("/session") {
            val attributeTexts = session.attributes.entries.map { it.key + " : " + it.value }

            response.addHeader ("attributes", attributeTexts.joinToString(", "))
            response.addHeader ("attribute values", session.attributes.values.joinToString(", "))
            response.addHeader ("attribute names", session.attributes.keys.joinToString(", "))

            response.addHeader ("creation",  session.creationTime.toString())
            response.addHeader ("id",  session.id ?: "")
            response.addHeader ("last access", session.lastAccessedTime.toString())
        }
    }

    fun attribute() {
        assert(client.put("/session/foo/bar").statusCode == 200)
        assertResponseEquals(client.get("/session/foo"), "bar")
    }

    fun sessionLifecycle() {
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

    override fun validate() {
        attribute()
        sessionLifecycle()
    }
}
