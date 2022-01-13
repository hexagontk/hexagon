package com.hexagonkt.http.server.handlers

import com.hexagonkt.core.require
import com.hexagonkt.http.model.ClientErrorStatus.METHOD_NOT_ALLOWED
import com.hexagonkt.http.model.ClientErrorStatus.NOT_FOUND
import com.hexagonkt.http.model.HttpMethod.GET
import com.hexagonkt.http.model.HttpMethod.PUT
import com.hexagonkt.http.model.SuccessStatus.*
import com.hexagonkt.http.server.model.HttpServerRequest
import com.hexagonkt.http.server.model.HttpServerResponse
import kotlinx.coroutines.runBlocking
import org.junit.jupiter.api.Test
import kotlin.test.assertEquals

internal class HttpServerTest {

    @Test fun `Handlers proof of concept`() = runBlocking {
        val path = PathHandler(
            OnHandler { ok() },
            AfterHandler { this },
            FilterHandler { next() },

            PathHandler("/a",
                OnHandler { notFound() },
                OnHandler("/{p}") {
                    send(HttpServerResponse(
                        status = OK,
                        body = pathParameters.require("p")
                    ))
                },

                PathHandler("/b",
                    OnHandler { send(status = METHOD_NOT_ALLOWED) },
                    OnHandler(GET) { send(status = NO_CONTENT) },
                    OnHandler("/{p}") {
                        send(HttpServerResponse(
                            status = OK,
                            body = pathParameters.require("p")
                        ))
                    }
                )
            )
        )

        assertEquals(NOT_FOUND, path.process(HttpServerRequest(path = "/a")).status)
        assertEquals(NO_CONTENT, path.process(HttpServerRequest(path = "/a/b")).status)
        assertEquals(METHOD_NOT_ALLOWED, path.process(HttpServerRequest(PUT, path = "/a/b")).status)
        assertEquals(OK, path.process(HttpServerRequest(path = "/a/x")).status)
        assertEquals("x", path.process(HttpServerRequest(path = "/a/x")).body)
        assertEquals(OK, path.process(HttpServerRequest(path = "/a/b/value")).status)
        assertEquals("value", path.process(HttpServerRequest(path = "/a/b/value")).body)
    }

    @Test fun `Builder proof of concept`() = runBlocking {

        val path = path {
            path("/a") {
                on { ok() }
                path("/b") {
                    on { send(status = ACCEPTED) }
                }
            }
        }

        assertEquals(OK, path.process(HttpServerRequest(path = "/a")).status)
        assertEquals(ACCEPTED, path.process(HttpServerRequest(path = "/a/b")).status)

        val contextPath = path("/p") {
            path("/a") {
                on { ok() }
                path("/b") {
                    on { send(status = ACCEPTED) }
                }
            }
        }

        assertEquals(NOT_FOUND, contextPath.process(HttpServerRequest(path = "/a")).status)
        assertEquals(OK, contextPath.process(HttpServerRequest(path = "/p/a")).status)
        assertEquals(ACCEPTED, contextPath.process(HttpServerRequest(path = "/p/a/b")).status)
    }
}
