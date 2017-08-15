package com.hexagonkt.client

//import com.hexagonkt.rest.files
import com.hexagonkt.serialization.serialize
import com.hexagonkt.server.Server
import com.hexagonkt.server.jetty.JettyServletEngine
import com.hexagonkt.settings.SettingsManager.settings
import org.asynchttpclient.Response
import org.testng.annotations.AfterClass
import org.testng.annotations.BeforeClass
import org.testng.annotations.Test

@Test class ClientTest {
    val server = Server(JettyServletEngine(), settings)
    val client by lazy {
        Client("http://${server.bindAddress.hostAddress}:${server.runtimePort}", "application/json")
    }

    @BeforeClass fun startup() {
//        server.files()

        server.router.apply {
            post {
                response.contentType = "application/json; charset=utf-8"
                ok(request.body)
            }

            get { ok(request.body) }
            head { ok(request.body) }
            put { ok(request.body) }
            delete { ok(request.body) }
            trace { ok(request.body) }
            options { ok(request.body) }
            patch { ok(request.body) }
        }

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
