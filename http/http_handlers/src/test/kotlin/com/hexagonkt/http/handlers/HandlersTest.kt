package com.hexagonkt.http.handlers

import com.hexagonkt.http.model.HttpRequest
import com.hexagonkt.http.model.INTERNAL_SERVER_ERROR_500
import com.hexagonkt.http.model.OK_200
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.reflect.KClass
import kotlin.test.assertNull

internal class HandlersTest {

    @Test fun `Root path is created properly from a list of handlers`() {
        assertEquals(PathHandler(""), path(handlers = emptyList()))
        assertEquals(PathHandler("/root"), path(handlers = listOf(PathHandler("/root"))))

        val expected = PathHandler("", listOf(OnHandler("/on") { this }))
        val actual = path(handlers = listOf(OnHandler("/on") { this }))
        assertEquals(expected.predicate, actual.predicate)
        assertEquals(expected.handlersPredicates(), actual.handlersPredicates())

        val expected2 = PathHandler("", listOf(OnHandler("/a") { this }, OnHandler("/b") { this }))
        val actual2 = path(handlers = listOf(OnHandler("/a") { this }, OnHandler("/b") { this }))
        assertEquals(expected2.predicate, actual2.predicate)
        assertEquals(expected2.handlersPredicates(), actual2.handlersPredicates())
    }

    @Test fun `Root path is created properly from a list of handlers and a prefix`() {
        assertEquals(PathHandler("/prefix"), path("/prefix", emptyList()))
        assertEquals(PathHandler("/prefix/root"), path("/prefix", listOf(PathHandler("/root"))))

        val expected = PathHandler("/prefix", listOf(OnHandler("/on") { this }))
        val actual = path("/prefix", listOf(OnHandler("/on") { this }))
        assertEquals(expected.predicate, actual.predicate)
        assertEquals(expected.handlersPredicates(), actual.handlersPredicates())

        val expected2 = PathHandler(
            "/prefix",
            listOf(OnHandler("/a") { this }, OnHandler("/b") { this })
        )
        val actual2 = path("/prefix", listOf(OnHandler("/a") { this }, OnHandler("/b") { this }))
        assertEquals(expected2.predicate, actual2.predicate)
        assertEquals(expected2.handlersPredicates(), actual2.handlersPredicates())
    }

    @Test
    @Suppress("CAST_NEVER_SUCCEEDS") // Required for test 'null' arguments
    fun `Exceptions are casted properly`() {
        assertFailsWith<IllegalStateException> { null.castException(Exception::class) }
        assertFailsWith<IllegalStateException> { null.castException(null as? KClass<Exception>) }
        assertFailsWith<ClassCastException> {
            IllegalStateException().castException(IllegalArgumentException::class)
        }
        assertFailsWith<IllegalStateException> {
            RuntimeException().castException(null as? KClass<Exception>)
        }

        val ise = IllegalStateException()
        assertEquals(ise, ise.castException(RuntimeException::class))
    }

    @Test fun `Exceptions are cleared properly`() {
        PathHandler(
            Exception<Exception> { ok() },
            OnHandler { error("Error") }
        )
        .process(HttpRequest())
        .let {
            assertEquals(OK_200, it.status)
            assertNull(it.exception)
        }

        PathHandler(
            Exception<Exception>(clear = false) { ok() },
            OnHandler { error("Error") }
        )
        .process(HttpRequest())
        .let {
            assertEquals(INTERNAL_SERVER_ERROR_500, it.status)
            assertEquals("Error", it.exception?.message)
            assert(it.exception is IllegalStateException)
        }
    }

    private fun PathHandler.handlersPredicates(): List<HttpPredicate> =
        handlers.map { it.handlerPredicate }
}
