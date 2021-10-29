package com.hexagonkt.http.server.examples

import com.hexagonkt.http.client.Client
import com.hexagonkt.http.client.Response
import com.hexagonkt.http.client.ahc.AhcAdapter
import com.hexagonkt.http.client.ClientSettings
import com.hexagonkt.http.server.Server
import com.hexagonkt.http.server.ServerPort
import org.junit.jupiter.api.AfterAll
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestInstance
import org.junit.jupiter.api.TestInstance.Lifecycle.PER_CLASS
import java.lang.System.nanoTime
import java.util.*

@TestInstance(PER_CLASS)
abstract class FiltersTest(adapter: ServerPort) {

    // filters
    private val users: Map<String, String> = mapOf(
        "Turing" to "London",
        "Dijkstra" to "Rotterdam"
    )

    private val server: Server = Server(adapter) {
        before { attributes["start"] = nanoTime() }

        before("/protected/*") {
            val authorization = request.headers["Authorization"] ?: halt(401, "Unauthorized")
            val credentials = authorization.removePrefix("Basic ")
            val userPassword = String(Base64.getDecoder().decode(credentials)).split(":")

            // Parameters set in call attributes are accessible in other filters and routes
            attributes["username"] = userPassword[0]
            attributes["password"] = userPassword[1]
        }

        // All matching filters are run in order unless call is halted
        before("/protected/*") {
            if(users[attributes["username"]] != attributes["password"])
                halt(403, "Forbidden")
        }

        get("/protected/hi") { ok("Hello ${attributes["username"]}!") }

        // After filters are ran even if request was halted before
        after { response.headers["time"] = nanoTime() - attributes["start"] as Long }
    }
    // filters

    private val client: Client by lazy {
        Client(AhcAdapter(), "http://localhost:${server.runtimePort}")
    }

    @BeforeAll fun initialize() {
        server.start()
    }

    @AfterAll fun shutdown() {
        server.stop()
    }

    @Test fun `Request without authorization returns 401`() {
        val response = client.get ("/protected/hi")
        assertResponseEquals(response, "Unauthorized", 401)
        assert(response.headers["time"]?.first()?.toLong() ?: 0 > 0)
    }

    @Test fun `HTTP request with valid credentials returns valid response`() {
        val endpoint = "http://localhost:${server.runtimePort}"
        val adapter = AhcAdapter()
        val settings = ClientSettings(user = "Turing", password = "London")
        val httpClient = Client(adapter, endpoint, settings)
        val response = httpClient.get ("/protected/hi")
        assertResponseEquals(response, "Hello Turing!", 200)
        assert(response.headers["time"]?.first()?.toLong() ?: 0 > 0)
    }

    @Test fun `Request with invalid password returns 403`() {
        val endpoint = "http://localhost:${server.runtimePort}"
        val adapter = AhcAdapter()
        val settings = ClientSettings(user = "Turing", password = "Millis")
        val httpClient = Client(adapter, endpoint, settings)
        val response = httpClient.get ("/protected/hi")
        assertResponseEquals(response, "Forbidden", 403)
        assert(response.headers["time"]?.first()?.toLong() ?: 0 > 0)
    }

    @Test fun `Request with invalid user returns 403`() {
        val endpoint = "http://localhost:${server.runtimePort}"
        val adapter = AhcAdapter()
        val settings = ClientSettings(user = "Curry", password = "Millis")
        val httpClient = Client(adapter, endpoint, settings)
        val response = httpClient.get ("/protected/hi")
        assertResponseEquals(response, "Forbidden", 403)
        assert(response.headers["time"]?.first()?.toLong() ?: 0 > 0)
    }

    private fun assertResponseEquals(response: Response?, content: String, status: Int = 200) {
        assert (response?.status == status)
        assert (response?.body == content)
    }
}
