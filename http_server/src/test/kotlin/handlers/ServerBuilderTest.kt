package com.hexagonkt.http.server.handlers

import com.hexagonkt.http.model.ClientErrorStatus.METHOD_NOT_ALLOWED
import com.hexagonkt.http.model.ClientErrorStatus.NOT_FOUND
import com.hexagonkt.http.model.HttpMethod.*
import com.hexagonkt.http.model.SuccessStatus.*
import com.hexagonkt.http.patterns.LiteralPathPattern
import com.hexagonkt.http.server.examples.send
import com.hexagonkt.http.server.model.HttpServerRequest
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.Test
import kotlin.test.assertEquals

@ExperimentalCoroutinesApi
internal class ServerBuilderTest {

    @Test fun `Adding prefix to a handler without path produces correct path pattern`() = runTest {

        val path = path("/c") {
            path("/books") {
                path("/{id}") {
                    get { ok("Title, Author") }
                    after(status = NOT_FOUND) { send(METHOD_NOT_ALLOWED) }
                }
            }
        }

        val response = path.send(GET, "/c/books/100")
        assertEquals(OK, response.status)
        assertEquals("Title, Author", response.bodyString())
    }

    @Test fun `Builder utility methods add all HTTP method handlers`() = runBlocking {

        val path = path {
            head { success(OK) }
            head("/head") { success(MULTI_STATUS) }
            trace { success(ALREADY_REPORTED) }
            trace("/trace") { success(ACCEPTED) }
            options { success(CREATED) }
            options("/options") { success(PARTIAL_CONTENT) }
            patch { success(RESET_CONTENT) }
            patch("/patch") { success(NO_CONTENT) }
        }

        assertEquals(OK, path.send(HEAD, "").status)
        assertEquals(MULTI_STATUS, path.send(HEAD, "/head").status)
        assertEquals(ALREADY_REPORTED, path.send(TRACE, "").status)
        assertEquals(ACCEPTED, path.send(TRACE, "/trace").status)
        assertEquals(CREATED, path.send(OPTIONS, "").status)
        assertEquals(PARTIAL_CONTENT, path.send(OPTIONS, "/options").status)
        assertEquals(RESET_CONTENT, path.send(PATCH, "").status)
        assertEquals(NO_CONTENT, path.send(PATCH, "/patch").status)
    }

    @Test fun `Builder utility methods add all types of handlers`() = runBlocking {

        val path = path {
            path("/a") {
                on(predicate("/b")) { ok("b") }
                on(setOf(GET), "/c") { ok("c") }
                on(GET, "/d") { ok("d") }
                on("/e") { ok("e") }

                after(predicate("/f")) { ok("f") }
                after(setOf(GET), "/g") { ok("g") }
                after(GET, "/h") { ok("h") }
                after("/i") { ok("i") }

                filter(predicate("/j")) { ok("j") }
                filter(setOf(GET), "/k") { ok("k") }
                filter(GET, "/l") { ok("l") }
                filter("/m") { ok("m") }
            }

            path("/n") { on(setOf(GET)) { ok("n") } }
            path("/o") { on(GET) { ok("o") } }
            path("/p") { on { ok("p") } }
            path("/q") { on(pattern = "") { ok("q") } }

            path("/r") { after(setOf(GET)) { ok("r") } }
            path("/s") { after(GET) { ok("s") } }
            path("/t") { after { ok("t") } }
            path("/u") { after(pattern = "") { ok("u") } }

            path("/v") { filter(setOf(GET)) { ok("v") } }
            path("/w") { filter(GET) { ok("w") } }
            path("/x") { filter { ok("x") } }
            path("/y") { filter(pattern = "") { ok("y") } }

            path("/z",
                OnHandler("/1") { ok("z1") },
                PathHandler("/2",
                    OnHandler("/1") { ok("z21") }
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

    @Test fun `Adding a path with a prefix of a previous path works as expected`() = runBlocking {
        val path = path {
            path("/a/b") { on(setOf(GET)) { ok("b") } }
            path("/a") {}
            path("/a") { on(GET, "/d") { ok("d") } }
            path("/a/c") { on(setOf(GET)) { ok("c") } }
            path("/b") { on(GET, "/d") { ok("d") } }
        }

        assertEquals("b", body(path, "/a/b"))
        assertEquals("c", body(path, "/a/c"))
        assertEquals("d", body(path, "/a/d"))
        assertEquals("d", body(path, "/b/d"))
    }

    private suspend fun body(path: PathHandler, value: String): String =
        path.process(HttpServerRequest(path = value)).body as String

    @Suppress("SameParameterValue") // Not relevant in tests
    private fun predicate(pattern: String): HttpServerPredicate =
        HttpServerPredicate(pathPattern = LiteralPathPattern(pattern))
}
