package com.hexagontk.http.handlers

import com.hexagontk.http.model.HttpMethod
import com.hexagontk.http.model.HttpMethod.*
import com.hexagontk.http.model.HttpMethod.Companion.ALL
import com.hexagontk.http.model.OK_200
import com.hexagontk.http.patterns.LiteralPathPattern
import com.hexagontk.http.model.HttpCall
import com.hexagontk.http.model.HttpRequest
import com.hexagontk.http.model.HttpResponse
import org.junit.jupiter.api.Test
import java.io.IOException
import kotlin.IllegalStateException
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

internal class HttpPredicateTest {

    @Test fun `Predicates with empty pattern matches exact path or root`() {
        val predicate = HttpPredicate()
        val call = HttpCall(HttpRequest(), HttpResponse())
        val context = HttpContext(call, predicate)
        assertTrue(predicate(context))
        val call1 = HttpCall(HttpRequest(path = "/"), HttpResponse())
        val context1 = HttpContext(call1, predicate)
        assertTrue(predicate(context1))
        val call2 = HttpCall(HttpRequest(path = "/a"), HttpResponse())
        val context2 = HttpContext(call2, predicate)
        assertFalse(predicate(context2))
    }

    @Test fun `HTTP predicates are described correctly`() {
        val predicate1 = HttpPredicate(setOf(PUT, OPTIONS), LiteralPathPattern("/a"))
        assertEquals("PUT, OPTIONS Literal '/a'", predicate1.describe())
        val predicate2 = HttpPredicate(setOf(GET), LiteralPathPattern("/b/c"))
        assertEquals("GET Literal '/b/c'", predicate2.describe())
        val predicate3 = HttpPredicate(pathPattern = LiteralPathPattern("/b/c"))
        assertEquals("ANY Literal '/b/c'", predicate3.describe())
        val predicate4 = HttpPredicate(setOf(POST, PATCH))
        assertEquals("POST, PATCH Literal ''", predicate4.describe())
    }

    @Test fun `Predicate without filter works properly`() {
        setOf(HttpPredicate(pattern = "*"), HttpPredicate(ALL, "*")).forEach {
            entries.forEach { method ->
                (100..599).forEach { status ->
                    listOf("/", "/a").forEach { pattern ->
                        assertTrue(it.predicate(serverContext(method, pattern, status)))
                        assertTrue(it.predicate(serverContext(method, pattern, status)))
                    }
                }
            }
        }
    }

    @Test fun `Predicate with method filter works properly`() {
        HttpPredicate(setOf(PUT, OPTIONS), "*").let {
            assertTrue(it.predicate(serverContext(PUT, "/a", OK_200)))
            assertTrue(it.predicate(serverContext(OPTIONS, "/a", OK_200)))
            assertFalse(it.predicate(serverContext(POST, "/", OK_200)))
            assertFalse(it.predicate(serverContext(GET, "/", OK_200)))
        }
    }

    @Test fun `Predicate with pattern filter works properly`() {
        HttpPredicate(pathPattern = LiteralPathPattern("/a")).let {
            assertTrue(it.predicate(serverContext(POST, "/a", OK_200)))
            assertTrue(it.predicate(serverContext(GET, "/a", OK_200)))
            assertFalse(it.predicate(serverContext(POST, "/", OK_200)))
            assertFalse(it.predicate(serverContext(GET, "/", OK_200)))
        }
    }

    @Test fun `Predicate with status filter works properly`() {
        HttpPredicate(pattern = "*", status = OK_200).let {
            entries.forEach { method ->
                listOf("/", "/a").forEach { pattern ->
                    assertTrue(it.predicate(serverContext(method, pattern, OK_200)))
                    ((100..599).toList() - OK_200).forEach { status ->
                        assertFalse(it.predicate(serverContext(method, pattern, status)))
                    }
                }
            }
        }
    }

    @Test fun `Predicate with combined filters (without method) works properly`() {
        HttpPredicate(
            pathPattern = LiteralPathPattern("/a"),
            status = OK_200).let {

            entries.forEach { method ->
                assertTrue(it.predicate(serverContext(method, "/a", OK_200, RuntimeException())))
                assertTrue(it.predicate(serverContext(method, "/a", OK_200, IllegalStateException())))
            }

            entries.forEach { method ->
                listOf("/b", "/c").forEach { pattern ->
                    listOf(null, IOException()).forEach { exception ->
                        ((100..599).toList() - OK_200).forEach { status ->
                            val c = serverContext(method, pattern, status, exception)
                            assertFalse(it.predicate(c))
                        }
                    }
                }
            }
        }

        HttpPredicate(pathPattern = LiteralPathPattern("/a")).let {
            entries.forEach { method ->
                (100..599).toList().forEach { status ->
                    listOf(RuntimeException(), IllegalStateException()).forEach { exception ->
                        assertTrue(it.predicate(serverContext(method, "/a", status, exception)))
                        assertTrue(it.predicate(serverContext(method, "/a", status, exception)))
                    }
                }
            }

            entries.forEach { method ->
                listOf("/b", "/c").forEach { pattern ->
                    listOf(null, IOException()).forEach { exception ->
                        (100..599).toList().forEach { status ->
                            val c = serverContext(method, pattern, status, exception)
                            assertFalse(it.predicate(c))
                        }
                    }
                }
            }
        }
    }

    @Test fun `Predicate with combined filters works properly`() {
        HttpPredicate(
            methods = setOf(POST, PUT),
            pathPattern = LiteralPathPattern("/a"),
            status = OK_200).let {

            setOf(POST, PUT).forEach { method ->
                assertTrue(it.predicate(serverContext(method, "/a", OK_200, RuntimeException())))
                assertTrue(it.predicate(serverContext(method, "/a", OK_200, IllegalStateException())))
            }
            (ALL - POST - PUT).forEach { method ->
                assertFalse(it.predicate(serverContext(method, "/a", OK_200, RuntimeException())))
                assertFalse(it.predicate(serverContext(method, "/a", OK_200, IllegalStateException())))
            }

            entries.forEach { method ->
                listOf("/b", "/c").forEach { pattern ->
                    listOf(null, IOException()).forEach { exception ->
                        ((100..599).toList() - OK_200).forEach { status ->
                            val c = it.predicate(serverContext(method, pattern, status, exception))
                            assertFalse(c)
                        }
                    }
                }
            }
        }
    }

    @Test fun `Add prefix to predicate`() {
        val p1 = HttpPredicate()
        assertEquals("/a", p1.addPrefix("/a").pathPattern.pattern)
        val p2 = HttpPredicate(pathPattern = LiteralPathPattern("/a"))
        assertEquals("/b/a", p2.addPrefix("/b").pathPattern.pattern)
    }

    private fun serverContext(
        method: HttpMethod,
        path: String,
        status: Int,
        exception: Exception? = null,
    ) =
        HttpContext(
            HttpCall(
                HttpRequest(method = method, path = path),
                HttpResponse(status = status)
            ),
            HttpPredicate(),
            exception = exception
        )
}
