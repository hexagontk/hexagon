package com.hexagonkt.handlers

import com.hexagonkt.handlers.HandlerTest.Companion.process
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test

internal class OnHandlerTest {

    private val path: ChainHandler<String> = ChainHandler(
        OnHandler({ it.event.matches(Regex("/after/resource")) }) { it.with("resource") },
        OnHandler({ it.event.matches(Regex("/after/\\d+")) }) { it.with("var:regex") },
        OnHandler({ it.event.matches(Regex("/after/123")) }) { it.with("unreachable") },
        OnHandler({ it.event.matches(Regex("/after/.*")) }) { it.with("var") },
    )

    @Test fun check() {
        kotlin.test.assertEquals("var:regex", path.process("/after/123"))
    }

    @Test fun `On handlers stop being processed after first match`() {
        assertEquals("resource", path.process("/after/resource"))
        assertEquals("var:regex", path.process("/after/123"))
        assertEquals("var", path.process("/after/abc"))
    }

    @Test fun `Only the first 'on' handler is processed`() {
        var flags = listOf(true, true, true, true)

        val chain = ChainHandler(
            BeforeHandler { it.with(event = "#") },
            OnHandler({ flags[0] }) { it.with(event = "a" + it.event) },
            OnHandler({ flags[1] }) { it.with(event = "b" + it.event) },
            OnHandler({ flags[2] }) { it.with(event = "c" + it.event) },
            OnHandler({ flags[3] }) { it.with(event = "d" + it.event) },
        )

        kotlin.test.assertEquals("a#", chain.process("_"))

        flags = listOf(true, false, false, false)
        kotlin.test.assertEquals("a#", chain.process("_"))

        flags = listOf(false, true, false, false)
        kotlin.test.assertEquals("b#", chain.process("_"))

        flags = listOf(false, false, true, false)
        kotlin.test.assertEquals("c#", chain.process("_"))

        flags = listOf(false, false, false, true)
        kotlin.test.assertEquals("d#", chain.process("_"))

        flags = listOf(false, false, false, false)
        kotlin.test.assertEquals("#", chain.process("_"))

        flags = listOf(false, true, true, false)
        kotlin.test.assertEquals("b#", chain.process("_"))

        flags = listOf(false, false, true, true)
        kotlin.test.assertEquals("c#", chain.process("_"))
    }
}
