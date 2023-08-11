package com.hexagonkt.handlers

import kotlin.IllegalStateException
import kotlin.system.measureNanoTime
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertIs
import kotlin.test.assertTrue
import com.hexagonkt.handlers.HandlerTest.Companion.process

internal class ChainHandlerTest {

    private val testChain = ChainHandler(
        AfterHandler { println("last"); it },
        AfterHandler { println("last2"); it },
        BeforeHandler { println("1"); it },
        BeforeHandler({ false }) { println("2"); it },
        ChainHandler(
            FilterHandler {
                println("3")
                val n = it.next()
                println("3")
                n
            },
            BeforeHandler { println("4"); it },
        )
    )

    @Test fun `Only the first 'on' handler is processed`() {
        var flags = listOf(true, true, true, true)

        val chain = ChainHandler(
            BeforeHandler { it.with(event = "#") },
            OnHandler({ flags[0] }) { it.with(event = "a" + it.event) },
            OnHandler({ flags[1] }) { it.with(event = "b" + it.event) },
            OnHandler({ flags[2] }) { it.with(event = "c" + it.event) },
            OnHandler({ flags[3] }) { it.with(event = "d" + it.event) },
        )

        assertEquals("a#", chain.process("_"))

        flags = listOf(true, false, false, false)
        assertEquals("a#", chain.process("_"))

        flags = listOf(false, true, false, false)
        assertEquals("b#", chain.process("_"))

        flags = listOf(false, false, true, false)
        assertEquals("c#", chain.process("_"))

        flags = listOf(false, false, false, true)
        assertEquals("d#", chain.process("_"))

        flags = listOf(false, false, false, false)
        assertEquals("#", chain.process("_"))

        flags = listOf(false, true, true, false)
        assertEquals("b#", chain.process("_"))

        flags = listOf(false, false, true, true)
        assertEquals("c#", chain.process("_"))
    }

    @Test fun `Build a nested chain of handlers`() {
        var flags = listOf(true, true, true, true)

        val chain = ChainHandler(
            BeforeHandler({ flags[0] }) { it.with(event = "a") },
            BeforeHandler({ flags[1] }) { it.with(event = "b") },
            BeforeHandler({ flags[2] }) { it.with(event = "c") },
            BeforeHandler({ flags[3] }) { it.with(event = "d") },
        )

        assertEquals("d", chain.process("_"))

        flags = listOf(true, false, false, false)
        assertEquals("a", chain.process("_"))

        flags = listOf(false, true, false, false)
        assertEquals("b", chain.process("_"))

        flags = listOf(false, false, true, false)
        assertEquals("c", chain.process("_"))

        flags = listOf(false, false, false, true)
        assertEquals("d", chain.process("_"))

        flags = listOf(false, false, false, false)
        assertEquals("_", chain.process("_"))

        flags = listOf(true, false, true, false)
        assertEquals("c", chain.process("_"))

        flags = listOf(true, false, false, true)
        assertEquals("d", chain.process("_"))
    }

    @Test fun `Chains of handlers pass the correct handler to context`() {

        val filterAE: (Context<String>) -> Boolean = { it.hasLetters('a', 'e') }
        val filterBF: (Context<String>) -> Boolean = { it.hasLetters('b', 'f') }
        val filterCG: (Context<String>) -> Boolean = { it.hasLetters('c', 'g') }
        val filterDH: (Context<String>) -> Boolean = { it.hasLetters('d', 'h') }

        val chainHandler =
            ChainHandler(
                AfterHandler(filterAE) {
                    assertEquals(filterAE, it.predicate)
                    it.appendText("<A1>")
                },
                AfterHandler(filterBF) {
                    assertEquals(filterBF, it.predicate)
                    it.appendText("<A2>")
                },
                BeforeHandler(filterCG) {
                    assertEquals(filterCG, it.predicate)
                    it.appendText("<B1>")
                },
                BeforeHandler(filterDH) {
                    assertEquals(filterDH, it.predicate)
                    it.appendText("<B2>")
                },
            )

        assertEquals("abcdefgh<B1><B2><A2><A1>", chainHandler.process("abcdefgh"))
    }

    @Test fun `Build a chain of handlers`() {

        fun createChainHandler() =
            ChainHandler(
                AfterHandler { it },
                BeforeHandler { it },
                ChainHandler(
                    FilterHandler { it.next() },
                    BeforeHandler { it },
                )
            )

        val t = time(5) { createChainHandler() }
        assertTrue(t < 5.0)

        val handlers = createChainHandler().handlers
        assertEquals(3, handlers.size)
        assertEquals(2, (handlers[2] as? ChainHandler<Any>)?.handlers?.size)

        val canonicalChain = ChainHandler(listOf(AfterHandler { it })) { false }
        assertEquals(1, canonicalChain.handlers.size)
        val listChain = ChainHandler(listOf(AfterHandler { it }))
        assertEquals(1, listChain.handlers.size)
    }

    @Test fun `Process event with chain of handlers ('delay' ignored)`() {
        val t = time(10) {
            assertEquals("/abc", testChain.process("/abc"))
        }

        assertTrue(t < 20.0)
    }

    @Test fun `Before and after handlers are called in order`() {

        val chains = listOf(
            ChainHandler<String>(
                AfterHandler({ it.hasLetters('a', 'e') }) { it.appendText("<A1>") },
                AfterHandler({ it.hasLetters('b', 'f') }) { it.appendText("<A2>") },
                BeforeHandler({ it.hasLetters('c', 'g') }) { it.appendText("<B1>") },
                BeforeHandler({ it.hasLetters('d', 'h') }) { it.appendText("<B2>") },
            ),
            ChainHandler<String>(
                BeforeHandler({ it.hasLetters('c', 'g') }) { it.appendText("<B1>") },
                AfterHandler({ it.hasLetters('a', 'e') }) { it.appendText("<A1>") },
                BeforeHandler({ it.hasLetters('d', 'h') }) { it.appendText("<B2>") },
                AfterHandler({ it.hasLetters('b', 'f') }) { it.appendText("<A2>") },
            ),
            ChainHandler<String>(
                BeforeHandler({ it.hasLetters('c', 'g') }) { it.appendText("<B1>") },
                BeforeHandler({ it.hasLetters('d', 'h') }) { it.appendText("<B2>") },
                AfterHandler({ it.hasLetters('a', 'e') }) { it.appendText("<A1>") },
                AfterHandler({ it.hasLetters('b', 'f') }) { it.appendText("<A2>") },
            ),
        )

        // No handler called
        chains.forEach {
            assertEquals("", it.process(""))
            assertEquals("_", it.process("_"))
        }

        // All handlers called
        chains.forEach {
            assertEquals("abcd<B1><B2><A2><A1>", it.process("abcd"))
            assertEquals("dcba<B1><B2><A2><A1>", it.process("dcba"))
            assertEquals("afch<B1><B2><A2><A1>", it.process("afch"))
            assertEquals("hcfa<B1><B2><A2><A1>", it.process("hcfa"))
            assertEquals("hcfa_<B1><B2><A2><A1>", it.process("hcfa_"))
        }

        // Single handler called
        chains.forEach {
            assertEquals("a<A1>", it.process("a"))
            assertEquals("e<A1>", it.process("e"))
            assertEquals("ae<A1>", it.process("ae"))
            assertEquals("a_<A1>", it.process("a_"))

            assertEquals("b<A2>", it.process("b"))
            assertEquals("f<A2>", it.process("f"))
            assertEquals("bf<A2>", it.process("bf"))
            assertEquals("b_<A2>", it.process("b_"))

            assertEquals("c<B1>", it.process("c"))
            assertEquals("g<B1>", it.process("g"))
            assertEquals("cg<B1>", it.process("cg"))
            assertEquals("c_<B1>", it.process("c_"))

            assertEquals("d<B2>", it.process("d"))
            assertEquals("h<B2>", it.process("h"))
            assertEquals("dh<B2>", it.process("dh"))
            assertEquals("d_<B2>", it.process("d_"))
        }

        // Two handlers called
        chains.forEach {
            assertEquals("af<A2><A1>", it.process("af"))
            assertEquals("bg<B1><A2>", it.process("bg"))
            assertEquals("ch<B1><B2>", it.process("ch"))
            assertEquals("de<B2><A1>", it.process("de"))
        }
    }

    @Test fun `Filters allow passing and halting`() {
        val chain = ChainHandler<String>(
            AfterHandler { it.appendText("<A1>") },
            BeforeHandler { it.appendText("<B1>")},
            FilterHandler({ it.hasLetters('a', 'b') }) {
                if (it.event.startsWith("a"))
                    it.with(event = it.event + "<PASS>").next()
                else
                    it.with(event = it.event + "<HALT>")
            },
            BeforeHandler { it.appendText("<B2>")},
        )

        // Filter passing
        assertEquals("a<B1><PASS><B2><A1>", chain.process("a"))
        // Filter halting
        assertEquals("b<B1><HALT><A1>", chain.process("b"))
        // Filter not matched
        assertEquals("c<B1><B2><A1>", chain.process("c"))
    }

    @Test fun `Chained handlers are executed as blocks`() {
        val chain = ChainHandler<String>(
            BeforeHandler { it.appendText("<B0>") },
            AfterHandler { it.appendText("<A0>") },
            ChainHandler({ it.hasLetters('a', 'b', 'c') },
                BeforeHandler { it.appendText("<B1>")},
                AfterHandler { it.appendText("<A1>") },
                FilterHandler({ it.hasLetters('a', 'b') }) {
                    if (it.event.startsWith("a"))
                        it.with(event = it.event + "<PASS>").next()
                    else
                        it.with(event = it.event + "<HALT>")
                },
                BeforeHandler { it.appendText("<B2>")},
            ),
        )

        // Chained filter
        assertEquals("a<B0><B1><PASS><B2><A1><A0>", chain.process("a"))
        // Filter halting
        assertEquals("b<B0><B1><HALT><A1><A0>", chain.process("b"))
        // Filter not matched
        assertEquals("c<B0><B1><B2><A1><A0>", chain.process("c"))

        // Nested chain unmatched
        assertEquals("d<B0><A0>", chain.process("d"))
    }

    @Test fun `Many chained handlers are processed properly`() {
        val chain = ChainHandler<String>(
            BeforeHandler { it.appendText("<B0>") },
            ChainHandler({ it.hasLetters('a', 'b') },
                BeforeHandler { it.appendText("<B1>")},
            ),
            ChainHandler({ it.hasLetters('a') },
                BeforeHandler { it.appendText("<B2>")},
            ),
            ChainHandler({ it.hasLetters('c') },
                BeforeHandler { it.appendText("<B3>")},
            ),
        )

        // Chained filter
        assertEquals("a<B0><B1><B2>", chain.process("a"))
        // Filter halting
        assertEquals("b<B0><B1>", chain.process("b"))
        // Filter not matched
        assertEquals("c<B0><B3>", chain.process("c"))

        // Nested chain unmatched
        assertEquals("d<B0>", chain.process("d"))
    }

    @Test fun `Chained handlers are executed as blocks bug`() {
        val chain = ChainHandler<String>(
            BeforeHandler { it.appendText("<B0>") },
            ChainHandler({ it.hasLetters('a', 'b', 'c') },
                BeforeHandler { it.appendText("<B1>")},
                FilterHandler({ it.hasLetters('a', 'b') }) {
                    if (it.event.startsWith("a"))
                        it.with(event = it.event + "<PASS>").next()
                    else
                        it.with(event = it.event + "<HALT>")
                },
                BeforeHandler { it.appendText("<B2>")},
                BeforeHandler { it.appendText("<A1>") },
            ),
            BeforeHandler { it.appendText("<A0>") },
        )

        // Chained filter
        assertEquals("a<B0><B1><PASS><B2><A1><A0>", chain.process("a"))
        // Filter halting
        assertEquals("b<B0><B1><HALT><A0>", chain.process("b"))
        // Filter not matched
        assertEquals("c<B0><B1><B2><A1><A0>", chain.process("c"))

        // Nested chain unmatched
        assertEquals("d<B0><A0>", chain.process("d"))
    }

    @Test fun `Exceptions don't prevent handlers execution`() {
        val chain = ChainHandler<String>(
            BeforeHandler { error("Fail") },
            AfterHandler { it.appendText("<A0>") },
            ChainHandler(
                { it.hasLetters('a', 'b', 'c') },
                BeforeHandler { it.appendText("<B1>") },
                AfterHandler {
                    assertTrue(it.exception is IllegalStateException)
                    it.appendText("<A1>")
                },
                FilterHandler({ it.hasLetters('a', 'b') }) {
                    if (it.event.startsWith("a"))
                        it.with(event = it.event + "<PASS>").next()
                    else
                        it.with(event = it.event + "<HALT>")
                },
                BeforeHandler { it.appendText("<B2>") },
            ),
        )

        // Chained filter
        assertEquals("a<B1><PASS><B2><A1><A0>", chain.process("a"))
        // Filter halting
        assertEquals("b<B1><HALT><A1><A0>", chain.process("b"))
        // Filter not matched
        assertEquals("c<B1><B2><A1><A0>", chain.process("c"))

        // Nested chain unmatched
        assertEquals("d<A0>", chain.process("d"))
    }

    @Test fun `Exceptions don't prevent handlers execution in after`() {
        val chain1 = ChainHandler<String>(
            AfterHandler { error("Fail") },
            AfterHandler { it.appendText("<A0>") },
        )

        assertEquals("a<A0>", chain1.process("a"))

        val chain2 = ChainHandler<String>(
            AfterHandler {
                assertTrue(it.exception is IllegalStateException)
                it.appendText("<A0>")
            },
            AfterHandler { error("Fail") },
        )

        assertEquals("a<A0>", chain2.process("a"))
    }

    @Test fun `Exceptions don't prevent handlers execution in filters`() {
        val chain = ChainHandler<String>(
            FilterHandler { error("Fail") },
            BeforeHandler { it.appendText("<B0>") },
        )

        val actual = chain.process(EventContext("a", chain.predicate))
        assertIs<IllegalStateException>(actual.exception)
        assertEquals("a", actual.event)
    }

    @Test fun `Context attributes are passed correctly`() {
        val chain = ChainHandler<String>(
            BeforeHandler { error("Fail") },
            AfterHandler { it.with(attributes = it.attributes + ("A0" to "A0")) },
            ChainHandler(
                { it.hasLetters('a', 'b', 'c') },
                BeforeHandler { it.with(attributes = it.attributes + ("B1" to "B1")) },
                AfterHandler {
                    assertTrue(it.exception is IllegalStateException)
                    it.with(attributes = it.attributes + ("A1" to "A1"))
                },
                FilterHandler({ it.hasLetters('a', 'b') }) {
                    if (it.event.startsWith("a"))
                        it.with(
                            event = it.event + "<PASS>",
                            attributes = it.attributes + ("passed" to true)
                        ).next()
                    else
                        it.with(event = it.event + "<HALT>")
                },
                BeforeHandler {
                    if (it.event.startsWith("a"))
                        assertEquals(true, it.attributes["passed"])
                    it.with(attributes = it.attributes + ("B2" to "B2") - "passed")
                },
            ),
        )

        // Chained filter
        val actual1 = chain.process(EventContext("a", chain.predicate))
        assertEquals("a<PASS>", actual1.event)
        val expected1 = mapOf<Any, Any>("B1" to "B1", "B2" to "B2", "A1" to "A1", "A0" to "A0")
        assertEquals(expected1, actual1.attributes)
        // Filter halting
        val actual2 = chain.process(EventContext("b", chain.predicate))
        assertEquals("b<HALT>", actual2.event)
        val expected2 = mapOf<Any, Any>("B1" to "B1", "A1" to "A1", "A0" to "A0")
        assertEquals(expected2, actual2.attributes)
        // Filter not matched
        val actual3 = chain.process(EventContext("c", chain.predicate))
        assertEquals("c", actual3.event)
        val expected3 = mapOf<Any, Any>("B1" to "B1", "B2" to "B2", "A1" to "A1", "A0" to "A0")
        assertEquals(expected3, actual3.attributes)

        // Nested chain unmatched
        val actual4 = chain.process(EventContext("d", chain.predicate))
        assertEquals("d", actual4.event)
        val expected4 = mapOf<Any, Any>("A0" to "A0")
        assertEquals(expected4, actual4.attributes)
    }

    private fun Context<String>.hasLetters(vararg letters: Char): Boolean =
        letters.any { event.contains(it) }

    private fun Context<String>.appendText(text: String): Context<String> =
        with(event = event + text)

    private fun time(times: Int, block: () -> Unit): Double =
        (0 until times).minOf {
            (measureNanoTime { block() } / 10e5).apply { println(">>> $this") }
        }
}
