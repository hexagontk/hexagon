package com.hexagonkt.http.handlers.async

import com.hexagonkt.core.require
import com.hexagonkt.handlers.async.done
import com.hexagonkt.http.model.METHOD_NOT_ALLOWED_405
import com.hexagonkt.http.model.NOT_FOUND_404
import com.hexagonkt.http.model.HttpMethod.GET
import com.hexagonkt.http.model.HttpMethod.PUT
import com.hexagonkt.http.model.*
import com.hexagonkt.http.model.HttpRequest
import com.hexagonkt.http.model.HttpResponse
import kotlin.test.Test
import kotlin.test.assertEquals

internal class HttpServerTest {

    @Test fun `Handlers proof of concept`() {
        val path = PathHandler(
            OnHandler { ok().done() },
            AfterHandler { this.done() },
            FilterHandler { nextContext() },

            PathHandler("/a",
                OnHandler { notFound().done() },
                OnHandler("/{p}") {
                    send(HttpResponse(
                        status = OK_200,
                        body = pathParameters.require("p")
                    )).done()
                },

                PathHandler("/b",
                    OnHandler { send(status = METHOD_NOT_ALLOWED_405).done() },
                    OnHandler(GET) { send(status = NO_CONTENT_204).done() },
                    OnHandler("/{p}") {
                        send(HttpResponse(
                            status = OK_200,
                            body = pathParameters.require("p")
                        )).done()
                    }
                )
            )
        )

        assertEquals(NOT_FOUND_404, path.process(HttpRequest(path = "/a")).join().status)
        assertEquals(NO_CONTENT_204, path.process(HttpRequest(path = "/a/b")).join().status)
        assertEquals(METHOD_NOT_ALLOWED_405, path.process(HttpRequest(PUT, path = "/a/b")).join().status)
        assertEquals(OK_200, path.process(HttpRequest(path = "/a/x")).join().status)
        assertEquals("x", path.process(HttpRequest(path = "/a/x")).join().response.body)
        assertEquals(OK_200, path.process(HttpRequest(path = "/a/b/value")).join().status)
        assertEquals("value", path.process(HttpRequest(path = "/a/b/value")).join().response.body)
    }

    @Test fun `Builder proof of concept`() {

        val path = path {
            path("/a") {
                on { ok().done() }
                path("/b") {
                    on { send(status = ACCEPTED_202).done() }
                }
            }
        }

        assertEquals(OK_200, path.process(HttpRequest(path = "/a")).join().status)
        assertEquals(ACCEPTED_202, path.process(HttpRequest(path = "/a/b")).join().status)

        val contextPath = path("/p") {
            path("/a") {
                on { ok().done() }
                path("/b") {
                    on { send(status = ACCEPTED_202).done() }
                }
            }
        }

        assertEquals(NOT_FOUND_404, contextPath.process(HttpRequest(path = "/a")).join().status)
        assertEquals(OK_200, contextPath.process(HttpRequest(path = "/p/a")).join().status)
        assertEquals(ACCEPTED_202, contextPath.process(HttpRequest(path = "/p/a/b")).join().status)
    }
}
