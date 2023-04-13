package com.hexagonkt.http.handlers.async

import com.hexagonkt.handlers.async.done
import com.hexagonkt.http.model.HttpMethod
import com.hexagonkt.http.model.HttpMethod.*
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

internal class PathHandlerTest {

    @Test fun `Paths are described correctly`() {
        val path = PathHandler(
            OnHandler(GET, "/get") { ok().done() },
            OnHandler(POST, "/post") { ok().done() },
            OnHandler(PUT, "/put") { ok().done() },

            PathHandler("/a",
                OnHandler(OPTIONS, "/options") { ok().done() },
                OnHandler(DELETE, "/delete") { ok().done() },
                OnHandler(HEAD, "/head") { ok().done() },

                PathHandler("/b",
                    OnHandler(PATCH, "/patch") { ok().done() },
                    OnHandler(TRACE, "/trace") { ok().done() },
                )
            )
        )

        val expectedDescription = """
            |GET, POST, PUT, OPTIONS, DELETE, HEAD, PATCH, TRACE Literal ''
            |    GET Literal '/get'
            |    POST Literal '/post'
            |    PUT Literal '/put'
            |    OPTIONS, DELETE, HEAD, PATCH, TRACE Literal (PREFIX) '/a'
            |        OPTIONS Literal '/options'
            |        DELETE Literal '/delete'
            |        HEAD Literal '/head'
            |        PATCH, TRACE Literal (PREFIX) '/b'
            |            PATCH Literal '/patch'
            |            TRACE Literal '/trace'
        """.trimMargin()
        val pathDescription = path.describe()
        assertEquals(expectedDescription, pathDescription)
    }

    @Test fun `Path handlers can be split by methods`() {
        val path = PathHandler(
            OnHandler(GET, "/get1") { ok().done() },
            AfterHandler(POST, "/post1") { ok().done() },
            FilterHandler(PUT, "/put1") { ok().done() },

            OnHandler(setOf(OPTIONS, DELETE), "/optionsDelete1") { ok().done() },

            OnHandler(GET, "/get2") { ok().done() },
            AfterHandler(POST, "/post2") { ok().done() },
            FilterHandler(PUT, "/put2") { ok().done() },

            OnHandler(setOf(OPTIONS, HEAD), "/optionsHead1") { ok().done() },
        )

        val pathsByMethod = path.byMethod().mapValues { it.value as PathHandler }

        assertEquals(setOf(GET, POST, PUT, OPTIONS, DELETE, HEAD), pathsByMethod.keys.toSet())
        pathsByMethod.forEach { (_, v) ->
            assertEquals(0, v.handlerPredicate.methods.size)
            assertTrue(v.handlers.all { it.handlerPredicate.methods.isEmpty() })
        }

        fun patterns(method: HttpMethod): List<String> = pathsByMethod[method]
            ?.handlers
            ?.map { it.handlerPredicate.pathPattern.pattern }
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
            OnHandler(GET, "/get1") { ok().done() },
            OnHandler(POST, "/post1") { ok().done() },
            OnHandler(PUT, "/put1") { ok().done() },
            OnHandler("/any1") { ok().done() },

            PathHandler("/a",
                OnHandler(setOf(GET, OPTIONS), "/options1") { ok().done() },
                OnHandler(DELETE, "/delete1") { ok().done() },
                OnHandler("/any2") { ok().done() },

                PathHandler("/b",
                    OnHandler(setOf(GET, PATCH), "/patch1") { ok().done() },
                    OnHandler(TRACE, "/trace1") { ok().done() },
                    OnHandler("/any3") { ok().done() },
                )
            )
        )

        val pathsByMethod = path.byMethod()

        fun PathHandler.patterns(): List<String> =
            handlers.flatMap {
                if (it is PathHandler) it.patterns()
                else listOf(it.handlerPredicate.pathPattern.pattern)
            }

        fun patterns(method: HttpMethod): List<String> =
            (pathsByMethod[method] as? PathHandler)?.patterns() ?: emptyList()

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
