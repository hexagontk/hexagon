package com.hexagonkt.http.server.handlers

import org.junit.jupiter.api.Test
import kotlin.test.assertEquals

internal class HandlersTest {

    @Test fun `Root path is created properly from a list of handlers`() {
        assertEquals(PathHandler(""), path(handlers = emptyList()))
        assertEquals(PathHandler("/root"), path(handlers = listOf(PathHandler("/root"))))

        val expected = PathHandler("", listOf(OnHandler("/on") { this }))
        val actual = path(handlers = listOf(OnHandler("/on") { this }))
        assertEquals(expected.serverPredicate, actual.serverPredicate)
        assertEquals(expected.handlersPredicates(), actual.handlersPredicates())

        val expected2 = PathHandler("", listOf(OnHandler("/a") { this }, OnHandler("/b") { this }))
        val actual2 = path(handlers = listOf(OnHandler("/a") { this }, OnHandler("/b") { this }))
        assertEquals(expected2.serverPredicate, actual2.serverPredicate)
        assertEquals(expected2.handlersPredicates(), actual2.handlersPredicates())
    }

    @Test fun `Root path is created properly from a list of handlers and a prefix`() {
        assertEquals(PathHandler("/prefix"), path("/prefix", emptyList()))
        assertEquals(PathHandler("/prefix/root"), path("/prefix", listOf(PathHandler("/root"))))

        val expected = PathHandler("/prefix", listOf(OnHandler("/on") { this }))
        val actual = path("/prefix", listOf(OnHandler("/on") { this }))
        assertEquals(expected.serverPredicate, actual.serverPredicate)
        assertEquals(expected.handlersPredicates(), actual.handlersPredicates())

        val expected2 = PathHandler(
            "/prefix",
            listOf(OnHandler("/a") { this }, OnHandler("/b") { this })
        )
        val actual2 = path("/prefix", listOf(OnHandler("/a") { this }, OnHandler("/b") { this }))
        assertEquals(expected2.serverPredicate, actual2.serverPredicate)
        assertEquals(expected2.handlersPredicates(), actual2.handlersPredicates())
    }

    private fun PathHandler.handlersPredicates(): List<HttpServerPredicate> =
        handlers.map { it.serverPredicate }
}
