package co.there4.hexagon.web

import co.there4.hexagon.web.jetty.JettyServer
import org.testng.annotations.AfterClass
import org.testng.annotations.BeforeClass
import org.testng.annotations.Test
import java.net.URL

@Test class ClientTest {
    val srv = JettyServer(bindPort = 5099)
    val client = Client(URL("http://localhost:5099"), "application/json")

    @BeforeClass fun startup() {
        srv.post { ok(request.body) }
        srv.get { ok(request.body) }
        srv.run()
    }

    @AfterClass fun shutdown() {
        srv.stop()
    }

    fun json_requests_works_as_expected() {
        val expectedBody = "{\n  \"foo\" : \"fighters\"\n}"

        val body = client.post("/", "application/json", mapOf ("foo" to "fighters")).responseBody
        assert(body.trim() == expectedBody)

        val body2 = client.post("/", body = mapOf ("foo" to "fighters")).responseBody
        assert(body2.trim() == expectedBody)

        client.get("/", "application/json") { "foo" }
        client.get("/") { "foo" }
    }
}
