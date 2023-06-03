package com.hexagonkt.http.handlers.async

import com.hexagonkt.handlers.async.done
import com.hexagonkt.http.model.METHOD_NOT_ALLOWED_405
import com.hexagonkt.http.model.NOT_FOUND_404
import com.hexagonkt.http.model.HttpMethod.*
import com.hexagonkt.http.model.*
import com.hexagonkt.http.patterns.LiteralPathPattern
import com.hexagonkt.http.handlers.async.examples.send
import com.hexagonkt.http.model.HttpRequest
import kotlin.test.Test
import kotlin.test.assertEquals

internal class HandlerBuilderTest {

    @Test fun `Adding prefix to a handler without path produces correct path pattern`() {

        val path = path("/c") {
            path("/books") {
                path("/{id}") {
                    get { ok("Title, Author").done() }
                    after(status = NOT_FOUND_404) { send(METHOD_NOT_ALLOWED_405).done() }
                }
            }
        }

        val response = path.send(GET, "/c/books/100")
        assertEquals(OK_200, response.status)
        assertEquals("Title, Author", response.bodyString())
    }

    @Test fun `Builder utility methods add all HTTP method handlers`() {

        val path = path {
            head { send(OK_200).done() }
            head("/head") { send(MULTI_STATUS_207).done() }
            trace { send(ALREADY_REPORTED_208).done() }
            trace("/trace") { send(ACCEPTED_202).done() }
            options { send(CREATED_201).done() }
            options("/options") { send(PARTIAL_CONTENT_206).done() }
            patch { send(RESET_CONTENT_205).done() }
            patch("/patch") { send(NO_CONTENT_204).done() }
        }

        assertEquals(OK_200, path.send(HEAD, "").status)
        assertEquals(MULTI_STATUS_207, path.send(HEAD, "/head").status)
        assertEquals(ALREADY_REPORTED_208, path.send(TRACE, "").status)
        assertEquals(ACCEPTED_202, path.send(TRACE, "/trace").status)
        assertEquals(CREATED_201, path.send(OPTIONS, "").status)
        assertEquals(PARTIAL_CONTENT_206, path.send(OPTIONS, "/options").status)
        assertEquals(RESET_CONTENT_205, path.send(PATCH, "").status)
        assertEquals(NO_CONTENT_204, path.send(PATCH, "/patch").status)
    }

    @Test fun `Builder utility methods add all types of handlers`() {

        val path = path {
            path("/a") {
                on(predicate("/b")) { ok("b").done() }
                on(setOf(GET), "/c") { ok("c").done() }
                on(GET, "/d") { ok("d").done() }
                on("/e") { ok("e").done() }

                after(predicate("/f")) { ok("f").done() }
                after(setOf(GET), "/g") { ok("g").done() }
                after(GET, "/h") { ok("h").done() }
                after("/i") { ok("i").done() }

                filter(predicate("/j")) { ok("j").done() }
                filter(setOf(GET), "/k") { ok("k").done() }
                filter(GET, "/l") { ok("l").done() }
                filter("/m") { ok("m").done() }
            }

            path("/n") { on(setOf(GET)) { ok("n").done() } }
            path("/o") { on(GET) { ok("o").done() } }
            path("/p") { on { ok("p").done() } }
            path("/q") { on(pattern = "") { ok("q").done() } }

            path("/r") { after(setOf(GET)) { ok("r").done() } }
            path("/s") { after(GET) { ok("s").done() } }
            path("/t") { after { ok("t").done() } }
            path("/u") { after(pattern = "") { ok("u").done() } }

            path("/v") { filter(setOf(GET)) { ok("v").done() } }
            path("/w") { filter(GET) { ok("w").done() } }
            path("/x") { filter { ok("x").done() } }
            path("/y") { filter(pattern = "") { ok("y").done() } }

            path("/z",
                OnHandler("/1") { ok("z1").done() },
                PathHandler("/2",
                    OnHandler("/1") { ok("z21").done() }
                )
            )
        }

        for (it in 'b'..'m')
            assertEquals(it.toString(), body(path, "/a/$it"))

        for (it in 'n'..'y')
            assertEquals(it.toString(), body(path, "/$it"))

        assertEquals("z1", body(path, "/z/1"))
        assertEquals("z21", body(path, "/z/2/1"))
    }

    @Test fun `Adding a path with a prefix of a previous path works as expected`() {
        val path = path {
            path("/a/b") { on(setOf(GET)) { ok("b").done() } }
            path("/a") {}
            path("/a") { on(GET, "/d") { ok("d").done() } }
            path("/a/c") { on(setOf(GET)) { ok("c").done() } }
            path("/b") { on(GET, "/d") { ok("d").done() } }
        }

        assertEquals("b", body(path, "/a/b"))
        assertEquals("c", body(path, "/a/c"))
        assertEquals("d", body(path, "/a/d"))
        assertEquals("d", body(path, "/b/d"))
    }

    private fun body(path: PathHandler, value: String): String =
        path.process(HttpRequest(path = value)).join().response.body as String

    @Suppress("SameParameterValue") // Not relevant in tests
    private fun predicate(pattern: String): HttpPredicate =
        HttpPredicate(pathPattern = LiteralPathPattern(pattern))
}
