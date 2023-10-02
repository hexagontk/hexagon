package com.hexagonkt.handlers

import com.hexagonkt.handlers.HandlerTest.Companion.process
import org.junit.jupiter.api.Test
import kotlin.test.assertEquals

internal class RoutingTest {

    private val path: ChainHandler<String> = ChainHandler(
        OnHandler({ it.event.matches(Regex("/after/resource")) }) { it.with("resource") },
        OnHandler({ it.event.matches(Regex("/after/\\d+")) }) { it.with("var:regex") },
        OnHandler({ it.event.matches(Regex("/after/123")) }) { it.with("unreachable") },
        OnHandler({ it.event.matches(Regex("/after/.*")) }) { it.with("var") },
    )

    @Test fun `On handlers stop being processed after first match`() {
        assertEquals("resource", path.process("/after/resource"))
        assertEquals("var:regex", path.process("/after/123"))
        assertEquals("var", path.process("/after/abc"))
    }
}
