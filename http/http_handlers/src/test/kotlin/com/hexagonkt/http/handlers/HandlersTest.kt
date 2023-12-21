package com.hexagonkt.http.handlers

import com.hexagonkt.http.model.HttpRequest
import com.hexagonkt.http.model.INTERNAL_SERVER_ERROR_500
import com.hexagonkt.http.model.NOT_FOUND_404
import com.hexagonkt.http.model.OK_200
import org.junit.jupiter.api.Test
import java.math.BigInteger
import java.time.LocalDate
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertContentEquals
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

    @Test fun `Exceptions are cleared properly`() {
        PathHandler(
            ExceptionHandler(Exception::class) { ok() },
            OnHandler { error("Error") }
        )
        .process(HttpRequest())
        .let {
            assertEquals(OK_200, it.status)
            assertNull(it.exception)
        }

        PathHandler(
            ExceptionHandler(Exception::class) { this },
            OnHandler { error("Error") }
        )
        .process(HttpRequest())
        .let {
            assertEquals(NOT_FOUND_404, it.status)
            assertNull(it.exception)
        }

        PathHandler(
            ExceptionHandler(Exception::class, clear = false) { ok() },
            OnHandler { error("Error") }
        )
        .process(HttpRequest())
        .let {
            assertEquals(INTERNAL_SERVER_ERROR_500, it.status)
            assertEquals("Error", it.exception?.message)
            assert(it.exception is IllegalStateException)
        }
    }

    @Test fun `Basic types can be converted to byte arrays to be sent as bodies`() {
        assertContentEquals("text".toByteArray(), bodyToBytes("text"))
        assertContentEquals("text".toByteArray(), bodyToBytes("text".toByteArray()))
        assertContentEquals(BigInteger.valueOf(42).toByteArray(), bodyToBytes(42))
        assertContentEquals(BigInteger.valueOf(1_234_567L).toByteArray(), bodyToBytes(1_234_567L))

        assertFailsWith<IllegalStateException> { bodyToBytes(LocalDate.now())  }
    }

    private fun PathHandler.handlersPredicates(): List<HttpPredicate> =
        handlers.map { it.handlerPredicate }
}
