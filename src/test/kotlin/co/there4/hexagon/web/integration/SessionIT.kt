package co.there4.hexagon.web.integration

import co.there4.hexagon.web.Server

@Suppress("unused") // Test methods are flagged as unused
class SessionIT : ItTest () {
    override fun initialize(srv: Server) {
        srv.put("/session/{key}/{value}") {
            session [request.parameter("key")] = request.parameter("value")
        }

        srv.get("/session/{key}") {
            ok (session [request.parameter("key")].toString())
        }

        srv.delete("/session/{key}") {
            session.remove(request.parameter("key"))
        }

        srv.get("/session") {
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
