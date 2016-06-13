package co.there4.hexagon.web.integration

import co.there4.hexagon.web.Server

@Suppress("unused") // Test methods are flagged as unused
class SessionIT : ItTest () {
    override fun initialize(server: Server) {
        server.put("/session/{key}/{value}") {
            session [request ["key"]] = request ["value"]
        }

        server.get("/session/{key}") {
            ok (session [request ["key"]].toString())
        }

        server.delete("/session/{key}") {
            session.remove(request ["key"])
        }

        server.get("/session") {
            val attributeTexts = session.attributes.entries.map { it.key + " : " + it.value }

            response.addHeader ("attributes", attributeTexts.joinToString(", "))
            response.addHeader ("attribute values", session.attributes.values.joinToString(", "))
            response.addHeader ("attribute names", session.attributes.keys.joinToString(", "))

            response.addHeader ("creation",  session.creationTime.toString())
            response.addHeader ("id",  session.id)
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
            assert(put("/session/foo/bar").statusCode == 200)
            assert(put("/session/foo/bazz").statusCode == 200)
            assert(put("/session/temporal/_").statusCode == 200)
            assert(delete("/session/temporal").statusCode == 200)

            assert(get("/session").statusCode == 200)
            assertResponseEquals(get("/session/foo"), 200, "bazz")
        }
    }
}
