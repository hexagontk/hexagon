package com.hexagonkt.http.server.examples

import com.hexagonkt.core.decodeBase64
import com.hexagonkt.http.model.ClientErrorStatus.FORBIDDEN
import com.hexagonkt.http.model.ClientErrorStatus.UNAUTHORIZED
import com.hexagonkt.http.model.Header
import com.hexagonkt.http.model.HttpMethod.GET
import com.hexagonkt.http.model.HttpMethod.PUT
import com.hexagonkt.http.model.SuccessStatus.*
import com.hexagonkt.http.server.handlers.PathHandler
import com.hexagonkt.http.server.handlers.path
import kotlin.test.Test
import kotlin.test.assertEquals

internal class FiltersTest {

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

    @Test fun `After handlers can be chained`() {
        assertEquals(ACCEPTED, path.send(GET, "/after").status)
        assertEquals(CREATED, path.send(GET, "/after/second").status)
        assertEquals(NO_CONTENT, path.send(PUT, "/after/second").status)
        assertEquals(ALREADY_REPORTED, path.send(PUT, "/after").status)
    }

    @Test fun `Request without authorization returns 401`() {
        val response = path.send(GET, "/protected/hi")
        val time = response.headers["time"]?.value?.toLong() ?: 0
        assertResponseEquals(response, "Unauthorized", UNAUTHORIZED)
        assert(time > 0)
    }

    @Test fun `HTTP request with valid credentials returns valid response`() {
        val response = path.send(GET, "/protected/hi", user = "Turing", password = "London")
        val time = response.headers["time"]?.value?.toLong() ?: 0
        assertResponseEquals(response, "Hello Turing!", OK)
        assert(time > 0)
    }

    @Test fun `Request with invalid password returns 403`() {
        val response = path.send(GET, "/protected/hi", user = "Turing", password = "Millis")
        val time = response.headers["time"]?.value?.toLong() ?: 0
        assertResponseEquals(response, "Forbidden", FORBIDDEN)
        assert(time > 0)
    }

    @Test fun `Request with invalid user returns 403`() {
        val response = path.send(GET, "/protected/hi", user = "Curry", password = "Millis")
        val time = response.headers["time"]?.value?.toLong() ?: 0
        assertResponseEquals(response, "Forbidden", FORBIDDEN)
        assert(time > 0)
    }
}
