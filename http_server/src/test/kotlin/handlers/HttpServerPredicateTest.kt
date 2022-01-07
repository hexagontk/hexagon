package com.hexagonkt.http.server.handlers

import com.hexagonkt.core.handlers.Context
import com.hexagonkt.http.model.HttpMethod
import com.hexagonkt.http.model.HttpMethod.*
import com.hexagonkt.http.model.HttpMethod.Companion.ALL
import com.hexagonkt.http.model.HttpStatus
import com.hexagonkt.http.model.SuccessStatus.OK
import com.hexagonkt.http.patterns.LiteralPathPattern
import com.hexagonkt.http.server.model.HttpServerCall
import com.hexagonkt.http.server.model.HttpServerRequest
import com.hexagonkt.http.server.model.HttpServerResponse
import kotlinx.coroutines.runBlocking
import org.junit.jupiter.api.Test
import java.io.IOException
import java.lang.IllegalStateException
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

internal class HttpServerPredicateTest {

    @Test fun `HTTP predicates are described correctly`() {
        val predicate1 = HttpServerPredicate(setOf(PUT, OPTIONS), LiteralPathPattern("/a"))
        assertEquals("PUT, OPTIONS LiteralPathPattern /a", predicate1.describe())
        val predicate2 = HttpServerPredicate(setOf(GET), LiteralPathPattern("/b/c"))
        assertEquals("GET LiteralPathPattern /b/c", predicate2.describe())
        val predicate3 = HttpServerPredicate(pathPattern = LiteralPathPattern("/b/c"))
        assertEquals("ANY LiteralPathPattern /b/c", predicate3.describe())
        val predicate4 = HttpServerPredicate(setOf(POST, PATCH))
        assertEquals("POST, PATCH <all paths>", predicate4.describe())
    }

    @Test fun `Predicate without filter works properly`() = runBlocking {
        setOf(HttpServerPredicate(), HttpServerPredicate(methods = ALL)).forEach {
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

    @Test fun `Predicate with method filter works properly`() = runBlocking {
        HttpServerPredicate(setOf(PUT, OPTIONS)).let {
            assertTrue(it.predicate(serverContext(PUT, "/a", OK)))
            assertTrue(it.predicate(serverContext(OPTIONS, "/a", OK)))
            assertFalse(it.predicate(serverContext(POST, "/", OK)))
            assertFalse(it.predicate(serverContext(GET, "/", OK)))
        }
    }

    @Test fun `Predicate with pattern filter works properly`() = runBlocking {
        HttpServerPredicate(pathPattern = LiteralPathPattern("/a")).let {
            assertTrue(it.predicate(serverContext(POST, "/a", OK)))
            assertTrue(it.predicate(serverContext(GET, "/a", OK)))
            assertFalse(it.predicate(serverContext(POST, "/", OK)))
            assertFalse(it.predicate(serverContext(GET, "/", OK)))
        }
    }

    @Test fun `Predicate with exception filter works properly`() = runBlocking {
        HttpServerPredicate(exception = RuntimeException::class).let {
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

    @Test fun `Predicate with status filter works properly`() = runBlocking {
        HttpServerPredicate(status = OK).let {
            HttpMethod.values().forEach { method ->
                listOf("/", "/a").forEach { pattern ->
                    assertTrue(it.predicate(serverContext(method, pattern, OK)))
                    (HttpStatus.codes.values.toList() - OK).forEach { status ->
                        assertFalse(it.predicate(serverContext(method, pattern, status)))
                    }
                }
            }
        }
    }

    @Test fun `Predicate with combined filters (without method) works properly`() = runBlocking {
        HttpServerPredicate(
            pathPattern = LiteralPathPattern("/a"),
            exception = RuntimeException::class,
            status = OK).let {

            HttpMethod.values().forEach { method ->
                assertTrue(it.predicate(serverContext(method, "/a", OK, RuntimeException())))
                assertTrue(it.predicate(serverContext(method, "/a", OK, IllegalStateException())))
            }

            HttpMethod.values().forEach { method ->
                listOf("/b", "/c").forEach { pattern ->
                    listOf(null, IOException()).forEach { exception ->
                        (HttpStatus.codes.values.toList() - OK).forEach { status ->
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

    @Test fun `Predicate with combined filters works properly`() = runBlocking {
        HttpServerPredicate(
            methods = setOf(POST, PUT),
            pathPattern = LiteralPathPattern("/a"),
            exception = RuntimeException::class,
            status = OK).let {

            setOf(POST, PUT).forEach { method ->
                assertTrue(it.predicate(serverContext(method, "/a", OK, RuntimeException())))
                assertTrue(it.predicate(serverContext(method, "/a", OK, IllegalStateException())))
            }
            (ALL - POST - PUT).forEach { method ->
                assertFalse(it.predicate(serverContext(method, "/a", OK, RuntimeException())))
                assertFalse(it.predicate(serverContext(method, "/a", OK, IllegalStateException())))
            }

            HttpMethod.values().forEach { method ->
                listOf("/b", "/c").forEach { pattern ->
                    listOf(null, IOException()).forEach { exception ->
                        (HttpStatus.codes.values.toList() - OK).forEach { status ->
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
        Context(
            HttpServerCall(
                HttpServerRequest(method = method, path = path),
                HttpServerResponse(status = status)
            ),
            HttpServerPredicate(),
            exception = exception
        )
}
