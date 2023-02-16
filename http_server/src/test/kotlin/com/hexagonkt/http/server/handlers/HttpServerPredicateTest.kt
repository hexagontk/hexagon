package com.hexagonkt.http.server.handlers

import com.hexagonkt.handlers.EventContext
import com.hexagonkt.http.model.HttpStatus
import com.hexagonkt.http.model.HttpMethod
import com.hexagonkt.http.model.HttpMethod.*
import com.hexagonkt.http.model.HttpMethod.Companion.ALL
import com.hexagonkt.http.model.OK_200
import com.hexagonkt.http.patterns.LiteralPathPattern
import com.hexagonkt.http.server.model.HttpServerCall
import com.hexagonkt.http.server.model.HttpServerRequest
import com.hexagonkt.http.server.model.HttpServerResponse
import kotlin.test.Test
import java.io.IOException
import kotlin.IllegalStateException
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

internal class HttpServerPredicateTest {

    @Test fun `Predicates with empty pattern matches exact path or root`() {
        val predicate = HttpServerPredicate()
        val call = HttpServerCall(HttpServerRequest(), HttpServerResponse())
        val context = EventContext(call, predicate)
        assertTrue(predicate(context))
        val call1 = HttpServerCall(HttpServerRequest(path = "/"), HttpServerResponse())
        val context1 = EventContext(call1, predicate)
        assertTrue(predicate(context1))
        val call2 = HttpServerCall(HttpServerRequest(path = "/a"), HttpServerResponse())
        val context2 = EventContext(call2, predicate)
        assertFalse(predicate(context2))
    }

    @Test fun `HTTP predicates are described correctly`() {
        val predicate1 = HttpServerPredicate(setOf(PUT, OPTIONS), LiteralPathPattern("/a"))
        assertEquals("PUT, OPTIONS Literal '/a'", predicate1.describe())
        val predicate2 = HttpServerPredicate(setOf(GET), LiteralPathPattern("/b/c"))
        assertEquals("GET Literal '/b/c'", predicate2.describe())
        val predicate3 = HttpServerPredicate(pathPattern = LiteralPathPattern("/b/c"))
        assertEquals("ANY Literal '/b/c'", predicate3.describe())
        val predicate4 = HttpServerPredicate(setOf(POST, PATCH))
        assertEquals("POST, PATCH Literal ''", predicate4.describe())
    }

    @Test fun `Predicate without filter works properly`() {
        setOf(HttpServerPredicate(pattern = "*"), HttpServerPredicate(ALL, "*")).forEach {
            HttpMethod.values().forEach { method ->
                HttpStatus.codes.values.forEach { status ->
                    listOf("/", "/a").forEach { pattern ->
                        assertTrue(it.predicate(serverContext(method, pattern, status)))
                        assertTrue(it.predicate(serverContext(method, pattern, status)))
                    }
                }
            }
        }
    }

    @Test fun `Predicate with method filter works properly`() {
        HttpServerPredicate(setOf(PUT, OPTIONS), "*").let {
            assertTrue(it.predicate(serverContext(PUT, "/a", OK_200)))
            assertTrue(it.predicate(serverContext(OPTIONS, "/a", OK_200)))
            assertFalse(it.predicate(serverContext(POST, "/", OK_200)))
            assertFalse(it.predicate(serverContext(GET, "/", OK_200)))
        }
    }

    @Test fun `Predicate with pattern filter works properly`() {
        HttpServerPredicate(pathPattern = LiteralPathPattern("/a")).let {
            assertTrue(it.predicate(serverContext(POST, "/a", OK_200)))
            assertTrue(it.predicate(serverContext(GET, "/a", OK_200)))
            assertFalse(it.predicate(serverContext(POST, "/", OK_200)))
            assertFalse(it.predicate(serverContext(GET, "/", OK_200)))
        }
    }

    @Test fun `Predicate with exception filter works properly`() {
        HttpServerPredicate(pattern = "*", exception = RuntimeException::class).let {
            HttpMethod.values().forEach { method ->
                HttpStatus.codes.values.forEach { status ->
                    listOf("/", "/a").forEach { pattern ->
                        val c1 = serverContext(method, pattern, status, RuntimeException())
                        assertTrue(it.predicate(c1))
                        val c2 = serverContext(method, pattern, status, IllegalStateException())
                        assertTrue(it.predicate(c2))
                        val c3 = serverContext(method, pattern, status, IOException())
                        assertFalse(it.predicate(c3))
                        val c4 = serverContext(method, pattern, status, null)
                        assertFalse(it.predicate(c4))
                    }
                }
            }
        }
    }

    @Test fun `Predicate with status filter works properly`() {
        HttpServerPredicate(pattern = "*", status = OK_200).let {
            HttpMethod.values().forEach { method ->
                listOf("/", "/a").forEach { pattern ->
                    assertTrue(it.predicate(serverContext(method, pattern, OK_200)))
                    (HttpStatus.codes.values.toList() - OK_200).forEach { status ->
                        assertFalse(it.predicate(serverContext(method, pattern, status)))
                    }
                }
            }
        }
    }

    @Test fun `Predicate with combined filters (without method) works properly`() {
        HttpServerPredicate(
            pathPattern = LiteralPathPattern("/a"),
            exception = RuntimeException::class,
            status = OK_200).let {

            HttpMethod.values().forEach { method ->
                assertTrue(it.predicate(serverContext(method, "/a", OK_200, RuntimeException())))
                assertTrue(it.predicate(serverContext(method, "/a", OK_200, IllegalStateException())))
            }

            HttpMethod.values().forEach { method ->
                listOf("/b", "/c").forEach { pattern ->
                    listOf(null, IOException()).forEach { exception ->
                        (HttpStatus.codes.values.toList() - OK_200).forEach { status ->
                            val c = serverContext(method, pattern, status, exception)
                            assertFalse(it.predicate(c))
                        }
                    }
                }
            }
        }

        HttpServerPredicate(
            pathPattern = LiteralPathPattern("/a"),
            exception = RuntimeException::class).let {

            HttpMethod.values().forEach { method ->
                HttpStatus.codes.values.toList().forEach { status ->
                    listOf(RuntimeException(), IllegalStateException()).forEach { exception ->
                        assertTrue(it.predicate(serverContext(method, "/a", status, exception)))
                        assertTrue(it.predicate(serverContext(method, "/a", status, exception)))
                    }
                }
            }

            HttpMethod.values().forEach { method ->
                listOf("/b", "/c").forEach { pattern ->
                    listOf(null, IOException()).forEach { exception ->
                        HttpStatus.codes.values.toList().forEach { status ->
                            val c = serverContext(method, pattern, status, exception)
                            assertFalse(it.predicate(c))
                        }
                    }
                }
            }
        }
    }

    @Test fun `Predicate with combined filters works properly`() {
        HttpServerPredicate(
            methods = setOf(POST, PUT),
            pathPattern = LiteralPathPattern("/a"),
            exception = RuntimeException::class,
            status = OK_200).let {

            setOf(POST, PUT).forEach { method ->
                assertTrue(it.predicate(serverContext(method, "/a", OK_200, RuntimeException())))
                assertTrue(it.predicate(serverContext(method, "/a", OK_200, IllegalStateException())))
            }
            (ALL - POST - PUT).forEach { method ->
                assertFalse(it.predicate(serverContext(method, "/a", OK_200, RuntimeException())))
                assertFalse(it.predicate(serverContext(method, "/a", OK_200, IllegalStateException())))
            }

            HttpMethod.values().forEach { method ->
                listOf("/b", "/c").forEach { pattern ->
                    listOf(null, IOException()).forEach { exception ->
                        (HttpStatus.codes.values.toList() - OK_200).forEach { status ->
                            val c = it.predicate(serverContext(method, pattern, status, exception))
                            assertFalse(c)
                        }
                    }
                }
            }
        }
    }

    @Test fun `Add prefix to predicate`() {
        val p1 = HttpServerPredicate()
        assertEquals("/a", p1.addPrefix("/a").pathPattern.pattern)
        val p2 = HttpServerPredicate(pathPattern = LiteralPathPattern("/a"))
        assertEquals("/b/a", p2.addPrefix("/b").pathPattern.pattern)
    }

    private fun serverContext(
        method: HttpMethod,
        path: String,
        status: HttpStatus,
        exception: Exception? = null,
    ) =
        EventContext(
            HttpServerCall(
                HttpServerRequest(method = method, path = path),
                HttpServerResponse(status = status)
            ),
            HttpServerPredicate(),
            exception = exception
        )
}
