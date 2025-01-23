package com.hexagontk.http.test.examples

import com.hexagontk.core.text.decodeBase64
import com.hexagontk.http.basicAuth
import com.hexagontk.http.client.HttpClient
import com.hexagontk.http.client.HttpClientPort
import com.hexagontk.http.client.HttpClientSettings
import com.hexagontk.http.model.FORBIDDEN_403
import com.hexagontk.http.model.UNAUTHORIZED_401
import com.hexagontk.http.model.HttpMethod.PUT
import com.hexagontk.http.model.*
import com.hexagontk.http.server.HttpServerPort
import com.hexagontk.http.server.HttpServerSettings
import com.hexagontk.http.handlers.PathHandler
import com.hexagontk.http.handlers.HttpHandler
import com.hexagontk.http.handlers.path
import com.hexagontk.http.test.BaseTest
import org.junit.jupiter.api.Test
import kotlin.test.assertEquals

@Suppress("FunctionName") // This class's functions are intended to be used only in tests
abstract class FiltersTest(
    final override val clientAdapter: () -> HttpClientPort,
    final override val serverAdapter: () -> HttpServerPort,
    final override val serverSettings: HttpServerSettings = HttpServerSettings(),
) : BaseTest() {

    // filters
    private val users: Map<String, String> = mapOf(
        "Turing" to "London",
        "Dijkstra" to "Rotterdam"
    )

    private val path: PathHandler = path {
        filter("*") {
            val start = System.nanoTime()
            // Call next and store result to chain it
            val next = next()
            val time = (System.nanoTime() - start).toString()
            // Copies result from chain with the extra data
            next.send(headers = response.headers + Header("time", time))
        }

        filter("/protected/*") {
            val authorization = request.authorization ?: return@filter unauthorized("Unauthorized")
            val credentials = authorization.body
            val userPassword = String(credentials.decodeBase64()).split(":")

            // Parameters set in call attributes are accessible in other filters and routes
            send(attributes = attributes
                + ("username" to userPassword[0])
                + ("password" to userPassword[1])
            ).next()
        }

        // All matching filters are run in order unless call is halted
        filter("/protected/*") {
            if(users[attributes["username"]] != attributes["password"])
                send(FORBIDDEN_403, "Forbidden")
            else
                next()
        }

        get("/protected/hi") {
            ok("Hello ${attributes["username"]}!")
        }

        path("/after") {
            after(PUT) {
                send(ALREADY_REPORTED_208)
            }

            after(PUT, "/second") {
                send(NO_CONTENT_204)
            }

            after("/second") {
                send(CREATED_201)
            }

            after {
                send(ACCEPTED_202)
            }
        }
    }
    // filters

    override val handler: HttpHandler = path

    @Test fun `After handlers can be chained`() {
        val server = server()
        val client = client(server)

        assertEquals(ACCEPTED_202, client.get("/after").status)
        assertEquals(CREATED_201, client.get("/after/second").status)
        assertEquals(NO_CONTENT_204, client.put("/after/second").status)
        assertEquals(ALREADY_REPORTED_208, client.put("/after").status)

        client.stop()
        server.stop()
    }

    @Test fun `Request without authorization returns 401`() {
        val server = server()
        val client = client(server)

        val response = client.get("/protected/hi")
        val time = response.headers["time"]?.text?.toLong() ?: 0
        assertResponseEquals(response, UNAUTHORIZED_401, "Unauthorized")
        assert(time > 0)

        client.stop()
        server.stop()
    }

    @Test fun `HTTP request with valid credentials returns valid response`() {
        authorizedClient("Turing", "London").use {
            val response = it.get("/protected/hi")
            val time = response.headers["time"]?.text?.toLong() ?: 0
            assertResponseEquals(response, OK_200, "Hello Turing!")
            assert(time > 0)
        }
    }

    @Test fun `Request with invalid password returns 403`() {
        authorizedClient("Turing", "Millis").use {
            val response = it.get("/protected/hi")
            val time = response.headers["time"]?.text?.toLong() ?: 0
            assertResponseEquals(response, FORBIDDEN_403, "Forbidden")
            assert(time > 0)
        }
    }

    @Test fun `Request with invalid user returns 403`() {
        authorizedClient("Curry", "Millis").use {
            val response = it.get("/protected/hi")
            val time = response.headers["time"]?.text?.toLong() ?: 0
            assertResponseEquals(response, FORBIDDEN_403, "Forbidden")
            assert(time > 0)
        }
    }

    private fun authorizedClient(user: String, password: String): HttpClient {
        val server = server()

        val settings = HttpClientSettings(
            baseUri = server.binding,
            authorization = Authorization("basic", basicAuth(user, password)),
        )
        return HttpClient(clientAdapter(), settings).apply { start() }
    }
}
