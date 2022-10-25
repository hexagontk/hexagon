package com.hexagonkt.http.test.examples

import com.hexagonkt.core.decodeBase64
import com.hexagonkt.http.client.HttpClient
import com.hexagonkt.http.client.HttpClientPort
import com.hexagonkt.http.client.HttpClientSettings
import com.hexagonkt.http.model.ClientErrorStatus.FORBIDDEN
import com.hexagonkt.http.model.ClientErrorStatus.UNAUTHORIZED
import com.hexagonkt.http.model.Header
import com.hexagonkt.http.model.HttpFields
import com.hexagonkt.http.model.HttpMethod.PUT
import com.hexagonkt.http.model.SuccessStatus.*
import com.hexagonkt.http.server.HttpServerPort
import com.hexagonkt.http.server.HttpServerSettings
import com.hexagonkt.http.server.handlers.PathHandler
import com.hexagonkt.http.server.handlers.HttpHandler
import com.hexagonkt.http.server.handlers.path
import com.hexagonkt.http.test.BaseTest
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
            val next = next()
            val time = (System.nanoTime() - start).toString()
            // Copies result from chain with the extra data
            next.send(headers = response.headers + Header("time", time))
        }

        filter("/protected/*") {
            val authorization = request.authorization ?: return@filter unauthorized("Unauthorized")
            val credentials = authorization.value
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
                send(FORBIDDEN, "Forbidden")
            else
                next()
        }

        get("/protected/hi") {
            ok("Hello ${attributes["username"]}!")
        }

        path("/after") {
            after(PUT) {
                success(ALREADY_REPORTED)
            }

            after(PUT, "/second") {
                success(NO_CONTENT)
            }

            after("/second") {
                success(CREATED)
            }

            after {
                success(ACCEPTED)
            }
        }
    }
    // filters

    override val handler: HttpHandler = path

    @Test fun `After handlers can be chained`() {
        assertEquals(ACCEPTED, client.get("/after").status)
        assertEquals(CREATED, client.get("/after/second").status)
        assertEquals(NO_CONTENT, client.put("/after/second").status)
        assertEquals(ALREADY_REPORTED, client.put("/after").status)
    }

    @Test fun `Request without authorization returns 401`() {
        val response = client.get("/protected/hi")
        val time = response.headers["time"]?.value?.toLong() ?: 0
        assertResponseEquals(response, UNAUTHORIZED, "Unauthorized")
        assert(time > 0)
    }

    @Test fun `HTTP request with valid credentials returns valid response`() {
        authorizedClient("Turing", "London").use {
            val response = it.get("/protected/hi")
            val time = response.headers["time"]?.value?.toLong() ?: 0
            assertResponseEquals(response, OK, "Hello Turing!")
            assert(time > 0)
        }
    }

    @Test fun `Request with invalid password returns 403`() {
        authorizedClient("Turing", "Millis").use {
            val response = it.get("/protected/hi")
            val time = response.headers["time"]?.value?.toLong() ?: 0
            assertResponseEquals(response, FORBIDDEN, "Forbidden")
            assert(time > 0)
        }
    }

    @Test fun `Request with invalid user returns 403`() {
        authorizedClient("Curry", "Millis").use {
            val response = it.get("/protected/hi")
            val time = response.headers["time"]?.value?.toLong() ?: 0
            assertResponseEquals(response, FORBIDDEN, "Forbidden")
            assert(time > 0)
        }
    }

    private fun authorizedClient(user: String, password: String): HttpClient {
        val headers = HttpFields(Header("authorization", basicAuth(user, password)))
        return HttpClient(
            clientAdapter(),
            URL("http://localhost:${server.runtimePort}"),
            HttpClientSettings(headers = headers)
        )
            .apply { start() }
    }
}
