package com.hexagonkt.http.server.handlers

import com.hexagonkt.http.model.HttpMethod
import com.hexagonkt.http.model.HttpMethod.*
import org.junit.jupiter.api.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

internal class PathHandlerTest {

    @Test fun `Paths are described correctly`() {
        val path = PathHandler(
            OnHandler(GET, "/get") { ok() },
            OnHandler(POST, "/post") { ok() },
            OnHandler(PUT, "/put") { ok() },

            PathHandler("/a",
                OnHandler(OPTIONS, "/options") { ok() },
                OnHandler(DELETE, "/delete") { ok() },
                OnHandler(HEAD, "/head") { ok() },

                PathHandler("/b",
                    OnHandler(PATCH, "/patch") { ok() },
                    OnHandler(TRACE, "/trace") { ok() },
                )
            )
        )

        val expectedDescription = """
            |GET, POST, PUT, OPTIONS, DELETE, HEAD, PATCH, TRACE <all paths>
            |    GET LiteralPathPattern /get
            |    POST LiteralPathPattern /post
            |    PUT LiteralPathPattern /put
            |    OPTIONS, DELETE, HEAD, PATCH, TRACE LiteralPathPattern (PREFIX) /a
            |        OPTIONS LiteralPathPattern /options
            |        DELETE LiteralPathPattern /delete
            |        HEAD LiteralPathPattern /head
            |        PATCH, TRACE LiteralPathPattern (PREFIX) /b
            |            PATCH LiteralPathPattern /patch
            |            TRACE LiteralPathPattern /trace
        """.trimMargin()
        val pathDescription = path.describe()
        assertEquals(expectedDescription, pathDescription)
    }

    @Test fun `Path handlers can be split by methods`() {
        val path = PathHandler(
            OnHandler(GET, "/get1") { ok() },
            AfterHandler(POST, "/post1") { ok() },
            FilterHandler(PUT, "/put1") { ok() },

            OnHandler(setOf(OPTIONS, DELETE), "/optionsDelete1") { ok() },

            OnHandler(GET, "/get2") { ok() },
            AfterHandler(POST, "/post2") { ok() },
            FilterHandler(PUT, "/put2") { ok() },

            OnHandler(setOf(OPTIONS, HEAD), "/optionsHead1") { ok() },
        )

        val pathsByMethod = path.byMethod()

        assertEquals(setOf(GET, POST, PUT, OPTIONS, DELETE, HEAD), pathsByMethod.keys.toSet())
        pathsByMethod.forEach { (_, v) ->
            assertEquals(0, v.serverPredicate.methods.size)
            assertTrue(v.handlers.all { it.serverPredicate.methods.isEmpty() })
        }

        fun patterns(method: HttpMethod): List<String> = pathsByMethod[method]
            ?.handlers
            ?.map { it.serverPredicate.pathPattern.pattern }
            ?: emptyList()

        assertEquals(listOf("/get1", "/get2"), patterns(GET))
        assertEquals(listOf("/post1", "/post2"), patterns(POST))
        assertEquals(listOf("/put1", "/put2"), patterns(PUT))
        assertEquals(listOf("/optionsDelete1", "/optionsHead1"), patterns(OPTIONS))
        assertEquals(listOf("/optionsDelete1"), patterns(DELETE))
        assertEquals(listOf("/optionsHead1"), patterns(HEAD))
    }

    @Test fun `Nested path handlers can be split by methods`() {
        val path = PathHandler(
            OnHandler(GET, "/get1") { ok() },
            OnHandler(POST, "/post1") { ok() },
            OnHandler(PUT, "/put1") { ok() },
            OnHandler("/any1") { ok() },

            PathHandler("/a",
                OnHandler(setOf(GET, OPTIONS), "/options1") { ok() },
                OnHandler(DELETE, "/delete1") { ok() },
                OnHandler("/any2") { ok() },

                PathHandler("/b",
                    OnHandler(setOf(GET, PATCH), "/patch1") { ok() },
                    OnHandler(TRACE, "/trace1") { ok() },
                    OnHandler("/any3") { ok() },
                )
            )
        )

        val pathsByMethod = path.byMethod()

        fun PathHandler.patterns(): List<String> =
            handlers.flatMap {
                if (it is PathHandler) it.patterns()
                else listOf(it.serverPredicate.pathPattern.pattern)
            }

        fun patterns(method: HttpMethod): List<String> =
            pathsByMethod[method]?.patterns() ?: emptyList()

        val getExpectedPatterns = listOf("/get1", "/any1", "/options1", "/any2", "/patch1", "/any3")
        assertEquals(getExpectedPatterns, patterns(GET))
        assertEquals(listOf("/post1", "/any1", "/any2", "/any3"), patterns(POST))
        assertEquals(listOf("/put1", "/any1", "/any2", "/any3"), patterns(PUT))
        assertEquals(listOf("/any1", "/options1", "/any2", "/any3"), patterns(OPTIONS))
        assertEquals(listOf("/any1", "/delete1", "/any2", "/any3"), patterns(DELETE))
        assertEquals(listOf("/any1", "/any2", "/patch1", "/any3"), patterns(PATCH))
        assertEquals(listOf("/any1", "/any2", "/trace1", "/any3"), patterns(TRACE))
    }
}
