package com.hexagonkt.http.test.async.examples

import com.hexagonkt.core.decodeBase64
import com.hexagonkt.handlers.async.done
import com.hexagonkt.http.client.HttpClient
import com.hexagonkt.http.client.HttpClientPort
import com.hexagonkt.http.client.HttpClientSettings
import com.hexagonkt.http.model.FORBIDDEN_403
import com.hexagonkt.http.model.UNAUTHORIZED_401
import com.hexagonkt.http.model.Header
import com.hexagonkt.http.model.Headers
import com.hexagonkt.http.model.HttpMethod.PUT
import com.hexagonkt.http.model.*
import com.hexagonkt.http.server.async.HttpServerPort
import com.hexagonkt.http.server.async.HttpServerSettings
import com.hexagonkt.http.handlers.async.PathHandler
import com.hexagonkt.http.handlers.async.HttpHandler
import com.hexagonkt.http.handlers.async.path
import com.hexagonkt.http.test.async.BaseTest
import org.junit.jupiter.api.Test
import java.net.URL
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
            val next = nextContext()
            val time = (System.nanoTime() - start).toString()
            // Copies result from chain with the extra data
            next.thenApply { it.send(headers = response.headers + Header("time", time)) }
        }

        filter("/protected/*") {
            val authorization = request.authorization ?: return@filter unauthorized("Unauthorized").done()
            val credentials = authorization.value
            val userPassword = String(credentials.decodeBase64()).split(":")

            // Parameters set in call attributes are accessible in other filters and routes
            send(attributes = attributes
                + ("username" to userPassword[0])
                + ("password" to userPassword[1])
            ).nextContext()
        }

        // All matching filters are run in order unless call is halted
        filter("/protected/*") {
            if(users[attributes["username"]] != attributes["password"])
                send(FORBIDDEN_403, "Forbidden").done()
            else
                nextContext()
        }

        get("/protected/hi") {
            ok("Hello ${attributes["username"]}!").done()
        }

        path("/after") {
            after(PUT) {
                send(ALREADY_REPORTED_208).done()
            }

            after(PUT, "/second") {
                send(NO_CONTENT_204).done()
            }

            after("/second") {
                send(CREATED_201).done()
            }

            after {
                send(ACCEPTED_202).done()
            }
        }
    }
    // filters

    override val handler: HttpHandler = path

    @Test fun `After handlers can be chained`() {
        assertEquals(ACCEPTED_202, client.get("/after").status)
        assertEquals(CREATED_201, client.get("/after/second").status)
        assertEquals(NO_CONTENT_204, client.put("/after/second").status)
        assertEquals(ALREADY_REPORTED_208, client.put("/after").status)
    }

    @Test fun `Request without authorization returns 401`() {
        val response = client.get("/protected/hi")
        val time = response.headers["time"]?.string()?.toLong() ?: 0
        assertResponseEquals(response, UNAUTHORIZED_401, "Unauthorized")
        assert(time > 0)
    }

    @Test fun `HTTP request with valid credentials returns valid response`() {
        authorizedClient("Turing", "London").use {
            val response = it.get("/protected/hi")
            val time = response.headers["time"]?.string()?.toLong() ?: 0
            assertResponseEquals(response, OK_200, "Hello Turing!")
            assert(time > 0)
        }
    }

    @Test fun `Request with invalid password returns 403`() {
        authorizedClient("Turing", "Millis").use {
            val response = it.get("/protected/hi")
            val time = response.headers["time"]?.string()?.toLong() ?: 0
            assertResponseEquals(response, FORBIDDEN_403, "Forbidden")
            assert(time > 0)
        }
    }

    @Test fun `Request with invalid user returns 403`() {
        authorizedClient("Curry", "Millis").use {
            val response = it.get("/protected/hi")
            val time = response.headers["time"]?.string()?.toLong() ?: 0
            assertResponseEquals(response, FORBIDDEN_403, "Forbidden")
            assert(time > 0)
        }
    }

    private fun authorizedClient(user: String, password: String): HttpClient {
        val settings = HttpClientSettings(
            baseUrl = URL("http://localhost:${server.runtimePort}"),
            headers = Headers(Header("authorization", basicAuth(user, password)))
        )
        return HttpClient(clientAdapter(), settings).apply { start() }
    }
}
