package com.hexagonkt.http.handlers.async.examples

import com.hexagonkt.core.decodeBase64
import com.hexagonkt.handlers.async.done
import com.hexagonkt.http.model.FORBIDDEN_403
import com.hexagonkt.http.model.UNAUTHORIZED_401
import com.hexagonkt.http.model.Header
import com.hexagonkt.http.model.HttpMethod.GET
import com.hexagonkt.http.model.HttpMethod.PUT
import com.hexagonkt.http.model.*
import com.hexagonkt.http.handlers.async.PathHandler
import com.hexagonkt.http.handlers.async.path
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

    @Test fun `After handlers can be chained`() {
        assertEquals(ACCEPTED_202, path.send(GET, "/after").status)
        assertEquals(CREATED_201, path.send(GET, "/after/second").status)
        assertEquals(NO_CONTENT_204, path.send(PUT, "/after/second").status)
        assertEquals(ALREADY_REPORTED_208, path.send(PUT, "/after").status)
    }

    @Test fun `Request without authorization returns 401`() {
        val response = path.send(GET, "/protected/hi")
        val time = response.headers["time"]?.value?.toLong() ?: 0
        assertResponseEquals(response, "Unauthorized", UNAUTHORIZED_401)
        assert(time > 0)
    }

    @Test fun `HTTP request with valid credentials returns valid response`() {
        val response = path.send(GET, "/protected/hi", user = "Turing", password = "London")
        val time = response.headers["time"]?.value?.toLong() ?: 0
        assertResponseEquals(response, "Hello Turing!", OK_200)
        assert(time > 0)
    }

    @Test fun `Request with invalid password returns 403`() {
        val response = path.send(GET, "/protected/hi", user = "Turing", password = "Millis")
        val time = response.headers["time"]?.value?.toLong() ?: 0
        assertResponseEquals(response, "Forbidden", FORBIDDEN_403)
        assert(time > 0)
    }

    @Test fun `Request with invalid user returns 403`() {
        val response = path.send(GET, "/protected/hi", user = "Curry", password = "Millis")
        val time = response.headers["time"]?.value?.toLong() ?: 0
        assertResponseEquals(response, "Forbidden", FORBIDDEN_403)
        assert(time > 0)
    }
}
