package co.there4.hexagon.server.integration

import co.there4.hexagon.server.Router
import co.there4.hexagon.server.ServerEngine

@Suppress("unused") // Test methods are flagged as unused
class SessionIT(serverEngine: ServerEngine) : ItTest (serverEngine) {
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
        withClients {
            assert(put("/session/foo/bar").statusCode == 200)
            assertResponseEquals(get("/session/foo"), 200, "bar")
        }
    }

    fun sessionLifecycle() {
        withClients {
            assert(get("/session/id").responseBody == "null")
            assert(get("/session/inactive").responseBody == "null")
            assert(get("/session/creation").responseBody == "null")
            assert(get("/session/access").responseBody == "null")
            assert(get("/session/new").responseBody == "true")

            assert(put("/session/foo/bar").statusCode == 200)
            assert(put("/session/foo/bazz").statusCode == 200)
            assert(put("/session/temporal/_").statusCode == 200)
            assert(delete("/session/temporal").statusCode == 200)

            assert(get("/session").statusCode == 200)
            assertResponseEquals(get("/session/foo"), 200, "bazz")

            assert(get("/session/id").responseBody != "null")
            assert(get("/session/inactive").responseBody != "null")
            assert(get("/session/creation").responseBody != "null")
            assert(get("/session/access").responseBody != "null")
            assert(get("/session/new").responseBody == "false")

            post("/session/invalidate")

            assert(get("/session/id").responseBody == "null")
            assert(get("/session/inactive").responseBody == "null")
            assert(get("/session/creation").responseBody == "null")
            assert(get("/session/access").responseBody == "null")
        }
    }

    override fun validate() {
        attribute()
        sessionLifecycle()
    }
}
