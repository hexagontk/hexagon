package co.there4.hexagon.web

import co.there4.hexagon.rest.files
import co.there4.hexagon.serialization.serialize
import co.there4.hexagon.web.servlet.JettyServletServer
import org.asynchttpclient.Response
import org.testng.annotations.AfterClass
import org.testng.annotations.BeforeClass
import org.testng.annotations.Test

@Test class ClientTest {
    val server = JettyServletServer()
    val client by lazy {
        Client("http://${server.bindAddress.hostAddress}:${server.runtimePort}", "application/json")
    }

    @BeforeClass fun startup() {
        server.files()

        server.post {
            response.contentType = "application/json; charset=utf-8"
            ok(request.body)
        }

        server.get { ok(request.body) }
        server.head { ok(request.body) }
        server.put { ok(request.body) }
        server.delete { ok(request.body) }
        server.trace { ok(request.body) }
        server.options { ok(request.body) }
        server.patch { ok(request.body) }

        server.run()
    }

    @AfterClass fun shutdown() {
        server.stop()
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

    fun http_methods_with_objects_work_ok() {
        val parameter = mapOf("key" to "value")
        checkResponse(client.get("/") { parameter }, parameter)
        checkResponse(client.head("/") { parameter }, null)
        checkResponse(client.post("/") { parameter }, parameter)
        checkResponse(client.put("/") { parameter }, parameter)
        checkResponse(client.delete("/") { parameter }, parameter)
        checkResponse(client.trace("/") { parameter }, parameter)
        checkResponse(client.options("/") { parameter }, parameter)
        checkResponse(client.patch("/") { parameter }, parameter)
    }

    fun files_can_be_fetched_and_downloaded() {}

    private fun checkResponse(response: Response, parameter: Map<String, String>?) {
        assert(response.statusCode == 200)
        assert(response.responseBody.trim() == parameter?.serialize()?.trim() ?: "")
    }
}
