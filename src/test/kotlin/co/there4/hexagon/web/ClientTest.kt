package co.there4.hexagon.web

import co.there4.hexagon.web.jetty.JettyServer
import org.testng.annotations.AfterClass
import org.testng.annotations.BeforeClass
import org.testng.annotations.Test

@Test class ClientTest {
    val srv = JettyServer(bindPort = 5099)
    val client = Client("http://localhost:5099", "application/json")

    @BeforeClass fun startup() {
        srv.post {
            response.contentType = "application/json; charset=utf-8"
            ok(request.body)
        }
        srv.get {
            ok(request.body)
        }
        srv.run()
    }

    @AfterClass fun shutdown() {
        srv.stop()
    }

    fun json_requests_works_as_expected() {
        val expectedBody = "{\n  \"foo\" : \"fighters\",\n  \"es\" : \"áéíóúÁÉÍÓÚñÑ\"\n}"
        val requestBody = mapOf("foo" to "fighters", "es" to "áéíóúÁÉÍÓÚñÑ")

        val body = client.post("/", "application/json", requestBody).responseBody
        assert(body.trim() == expectedBody)

        val body2 = client.post("/", body = requestBody).responseBody
        assert(body2.trim() == expectedBody)

        client.get("/", "application/json") { "foo" }
        client.get("/") { "foo" }
    }
}
