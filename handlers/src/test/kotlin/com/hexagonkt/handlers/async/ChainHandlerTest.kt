package com.hexagonkt.handlers.async

import kotlin.IllegalStateException
import kotlin.system.measureNanoTime
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertIs
import kotlin.test.assertTrue
import com.hexagonkt.handlers.async.HandlerTest.Companion.process

internal class ChainHandlerTest {

    private val testChain = ChainHandler(
        AfterHandler { println("last"); it.done() },
        AfterHandler { println("last2"); it.done() },
        OnHandler { println("1"); it.done() },
        OnHandler({ false }) { println("2"); it.done() },
        ChainHandler(
            FilterHandler {
                println("3")
                val n = it.next()
                println("3")
                n
            },
            OnHandler { println("4"); it.done() },
        )
    )

    @Test fun `Build a nested chain of handlers`() {
        var flags = listOf(true, true, true, true)

        val chain = ChainHandler(
            OnHandler({ flags[0] }) { it.with(event = "a").done() },
            OnHandler({ flags[1] }) { it.with(event = "b").done() },
            OnHandler({ flags[2] }) { it.with(event = "c").done() },
            OnHandler({ flags[3] }) { it.with(event = "d").done() },
        )

        assertEquals("d", chain.process("_").get())

        flags = listOf(true, false, false, false)
        assertEquals("a", chain.process("_").get())

        flags = listOf(false, true, false, false)
        assertEquals("b", chain.process("_").get())

        flags = listOf(false, false, true, false)
        assertEquals("c", chain.process("_").get())

        flags = listOf(false, false, false, true)
        assertEquals("d", chain.process("_").get())

        flags = listOf(false, false, false, false)
        assertEquals("_", chain.process("_").get())

        flags = listOf(true, false, true, false)
        assertEquals("c", chain.process("_").get())

        flags = listOf(true, false, false, true)
        assertEquals("d", chain.process("_").get())
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
                    it.appendText("<A1>").done()
                },
                AfterHandler(filterBF) {
                    assertEquals(filterBF, it.predicate)
                    it.appendText("<A2>").done()
                },
                OnHandler(filterCG) {
                    assertEquals(filterCG, it.predicate)
                    it.appendText("<B1>").done()
                },
                OnHandler(filterDH) {
                    assertEquals(filterDH, it.predicate)
                    it.appendText("<B2>").done()
                },
            )

        assertEquals("abcdefgh<B1><B2><A2><A1>", chainHandler.process("abcdefgh").get())
    }

    @Test fun `Build a chain of handlers`() {

        fun createChainHandler() =
            ChainHandler(
                AfterHandler { it.done() },
                OnHandler { it.done() },
                ChainHandler(
                    FilterHandler { it.next() },
                    OnHandler { it.done() },
                )
            )

        val t = time(5) { createChainHandler() }
        assertTrue(t < 5.0)

        val handlers = createChainHandler().handlers
        assertEquals(3, handlers.size)
        assertEquals(2, (handlers[2] as? ChainHandler<Any>)?.handlers?.size)

        val canonicalChain = ChainHandler(listOf(AfterHandler { it.done() })) { false }
        assertEquals(1, canonicalChain.handlers.size)
        val listChain = ChainHandler(listOf(AfterHandler { it.done() }))
        assertEquals(1, listChain.handlers.size)
    }

    @Test fun `Process event with chain of handlers ('delay' ignored)`() {
        val t = time(5) {
            assertEquals("/abc", testChain.process("/abc").get())
        }

        assertTrue(t < 10.0)
    }

    @Test fun `Before and after handlers are called in order`() {

        val chains = listOf(
            ChainHandler<String>(
                AfterHandler({ it.hasLetters('a', 'e') }) { it.appendText("<A1>").done() },
                AfterHandler({ it.hasLetters('b', 'f') }) { it.appendText("<A2>").done() },
                OnHandler({ it.hasLetters('c', 'g') }) { it.appendText("<B1>").done() },
                OnHandler({ it.hasLetters('d', 'h') }) { it.appendText("<B2>").done() },
            ),
            ChainHandler<String>(
                OnHandler({ it.hasLetters('c', 'g') }) { it.appendText("<B1>").done() },
                AfterHandler({ it.hasLetters('a', 'e') }) { it.appendText("<A1>").done() },
                OnHandler({ it.hasLetters('d', 'h') }) { it.appendText("<B2>").done() },
                AfterHandler({ it.hasLetters('b', 'f') }) { it.appendText("<A2>").done() },
            ),
            ChainHandler<String>(
                OnHandler({ it.hasLetters('c', 'g') }) { it.appendText("<B1>").done() },
                OnHandler({ it.hasLetters('d', 'h') }) { it.appendText("<B2>").done() },
                AfterHandler({ it.hasLetters('a', 'e') }) { it.appendText("<A1>").done() },
                AfterHandler({ it.hasLetters('b', 'f') }) { it.appendText("<A2>").done() },
            ),
        )

        // No handler called
        chains.forEach {
            assertEquals("", it.process("").get())
            assertEquals("_", it.process("_").get())
        }

        // All handlers called
        chains.forEach {
            assertEquals("abcd<B1><B2><A2><A1>", it.process("abcd").get())
            assertEquals("dcba<B1><B2><A2><A1>", it.process("dcba").get())
            assertEquals("afch<B1><B2><A2><A1>", it.process("afch").get())
            assertEquals("hcfa<B1><B2><A2><A1>", it.process("hcfa").get())
            assertEquals("hcfa_<B1><B2><A2><A1>", it.process("hcfa_").get())
        }

        // Single handler called
        chains.forEach {
            assertEquals("a<A1>", it.process("a").get())
            assertEquals("e<A1>", it.process("e").get())
            assertEquals("ae<A1>", it.process("ae").get())
            assertEquals("a_<A1>", it.process("a_").get())

            assertEquals("b<A2>", it.process("b").get())
            assertEquals("f<A2>", it.process("f").get())
            assertEquals("bf<A2>", it.process("bf").get())
            assertEquals("b_<A2>", it.process("b_").get())

            assertEquals("c<B1>", it.process("c").get())
            assertEquals("g<B1>", it.process("g").get())
            assertEquals("cg<B1>", it.process("cg").get())
            assertEquals("c_<B1>", it.process("c_").get())

            assertEquals("d<B2>", it.process("d").get())
            assertEquals("h<B2>", it.process("h").get())
            assertEquals("dh<B2>", it.process("dh").get())
            assertEquals("d_<B2>", it.process("d_").get())
        }

        // Two handlers called
        chains.forEach {
            assertEquals("af<A2><A1>", it.process("af").get())
            assertEquals("bg<B1><A2>", it.process("bg").get())
            assertEquals("ch<B1><B2>", it.process("ch").get())
            assertEquals("de<B2><A1>", it.process("de").get())
        }
    }

    @Test fun `Filters allow passing and halting`() {
        val chain = ChainHandler<String>(
            AfterHandler { it.appendText("<A1>").done() },
            OnHandler { it.appendText("<B1>").done() },
            FilterHandler({ it.hasLetters('a', 'b') }) {
                if (it.event.startsWith("a"))
                    it.with(event = it.event + "<PASS>").next()
                else
                    it.with(event = it.event + "<HALT>").done()
            },
            OnHandler { it.appendText("<B2>").done() },
        )

        // Filter passing
        assertEquals("a<B1><PASS><B2><A1>", chain.process("a").get())
        // Filter halting
        assertEquals("b<B1><HALT><A1>", chain.process("b").get())
        // Filter not matched
        assertEquals("c<B1><B2><A1>", chain.process("c").get())
    }

    @Test fun `Chained handlers are executed as blocks`() {
        val chain = ChainHandler<String>(
            OnHandler { it.appendText("<B0>").done() },
            AfterHandler { it.appendText("<A0>").done() },
            ChainHandler({ it.hasLetters('a', 'b', 'c') },
                OnHandler { it.appendText("<B1>").done() },
                AfterHandler { it.appendText("<A1>").done() },
                FilterHandler({ it.hasLetters('a', 'b') }) {
                    if (it.event.startsWith("a"))
                        it.with(event = it.event + "<PASS>").next()
                    else
                        it.with(event = it.event + "<HALT>").done()
                },
                OnHandler { it.appendText("<B2>").done() },
            ),
        )

        // Chained filter
        assertEquals("a<B0><B1><PASS><B2><A1><A0>", chain.process("a").get())
        // Filter halting
        assertEquals("b<B0><B1><HALT><A1><A0>", chain.process("b").get())
        // Filter not matched
        assertEquals("c<B0><B1><B2><A1><A0>", chain.process("c").get())

        // Nested chain unmatched
        assertEquals("d<B0><A0>", chain.process("d").get())
    }

    @Test fun `Many chained handlers are processed properly`() {
        val chain = ChainHandler<String>(
            OnHandler { it.appendText("<B0>").done() },
            ChainHandler({ it.hasLetters('a', 'b') },
                OnHandler { it.appendText("<B1>").done() },
            ),
            ChainHandler({ it.hasLetters('a') },
                OnHandler { it.appendText("<B2>").done() },
            ),
            ChainHandler({ it.hasLetters('c') },
                OnHandler { it.appendText("<B3>").done() },
            ),
        )

        // Chained filter
        assertEquals("a<B0><B1><B2>", chain.process("a").get())
        // Filter halting
        assertEquals("b<B0><B1>", chain.process("b").get())
        // Filter not matched
        assertEquals("c<B0><B3>", chain.process("c").get())

        // Nested chain unmatched
        assertEquals("d<B0>", chain.process("d").get())
    }

    @Test fun `Chained handlers are executed as blocks bug`() {
        val chain = ChainHandler<String>(
            OnHandler { it.appendText("<B0>").done() },
            ChainHandler({ it.hasLetters('a', 'b', 'c') },
                OnHandler { it.appendText("<B1>").done() },
                FilterHandler({ it.hasLetters('a', 'b') }) {
                    if (it.event.startsWith("a"))
                        it.with(event = it.event + "<PASS>").next()
                    else
                        it.with(event = it.event + "<HALT>").done()
                },
                OnHandler { it.appendText("<B2>").done() },
                OnHandler { it.appendText("<A1>").done() },
            ),
            OnHandler { it.appendText("<A0>").done() },
        )

        // Chained filter
        assertEquals("a<B0><B1><PASS><B2><A1><A0>", chain.process("a").get())
        // Filter halting
        assertEquals("b<B0><B1><HALT><A0>", chain.process("b").get())
        // Filter not matched
        assertEquals("c<B0><B1><B2><A1><A0>", chain.process("c").get())

        // Nested chain unmatched
        assertEquals("d<B0><A0>", chain.process("d").get())
    }

    @Test fun `Exceptions don't prevent handlers execution`() {
        val chain = ChainHandler<String>(
            OnHandler { error("Fail") },
            AfterHandler { it.appendText("<A0>").done() },
            ChainHandler(
                { it.hasLetters('a', 'b', 'c') },
                OnHandler { it.appendText("<B1>").done() },
                AfterHandler {
                    assertTrue(it.exception is IllegalStateException)
                    it.appendText("<A1>").done()
                },
                FilterHandler({ it.hasLetters('a', 'b') }) {
                    if (it.event.startsWith("a"))
                        it.with(event = it.event + "<PASS>").next()
                    else
                        it.with(event = it.event + "<HALT>").done()
                },
                OnHandler { it.appendText("<B2>").done() },
            ),
        )

        // Chained filter
        assertEquals("a<B1><PASS><B2><A1><A0>", chain.process("a").get())
        // Filter halting
        assertEquals("b<B1><HALT><A1><A0>", chain.process("b").get())
        // Filter not matched
        assertEquals("c<B1><B2><A1><A0>", chain.process("c").get())

        // Nested chain unmatched
        assertEquals("d<A0>", chain.process("d").get())
    }

    @Test fun `Exceptions don't prevent handlers execution in after`() {
        val chain1 = ChainHandler<String>(
            AfterHandler { error("Fail") },
            AfterHandler { it.appendText("<A0>").done() },
        )

        assertEquals("a<A0>", chain1.process("a").get())

        val chain2 = ChainHandler<String>(
            AfterHandler {
                assertTrue(it.exception is IllegalStateException)
                it.appendText("<A0>").done()
            },
            AfterHandler { error("Fail") },
        )

        assertEquals("a<A0>", chain2.process("a").get())
    }

    @Test fun `Exceptions don't prevent handlers execution in filters`() {
        val chain = ChainHandler<String>(
            FilterHandler { error("Fail") },
            OnHandler { it.appendText("<B0>").done() },
        )

        val actual = chain.process(EventContext("a", chain.predicate)).get()
        assertIs<IllegalStateException>(actual.exception)
        assertEquals("a", actual.event)
    }

    @Test fun `Context attributes are passed correctly`() {
        val chain = ChainHandler<String>(
            OnHandler { error("Fail") },
            AfterHandler { it.with(attributes = it.attributes + ("A0" to "A0")).done() },
            ChainHandler(
                { it.hasLetters('a', 'b', 'c') },
                OnHandler { it.with(attributes = it.attributes + ("B1" to "B1")).done() },
                AfterHandler {
                    assertTrue(it.exception is IllegalStateException)
                    it.with(attributes = it.attributes + ("A1" to "A1")).done()
                },
                FilterHandler({ it.hasLetters('a', 'b') }) {
                    if (it.event.startsWith("a"))
                        it.with(
                            event = it.event + "<PASS>",
                            attributes = it.attributes + ("passed" to true)
                        ).next()
                    else
                        it.with(event = it.event + "<HALT>").done()
                },
                OnHandler {
                    if (it.event.startsWith("a"))
                        assertEquals(true, it.attributes["passed"])
                    it.with(attributes = it.attributes + ("B2" to "B2") - "passed").done()
                },
            ),
        )

        // Chained filter
        val actual1 = chain.process(EventContext("a", chain.predicate)).get()
        assertEquals("a<PASS>", actual1.event)
        val expected1 = mapOf<Any, Any>("B1" to "B1", "B2" to "B2", "A1" to "A1", "A0" to "A0")
        assertEquals(expected1, actual1.attributes)
        // Filter halting
        val actual2 = chain.process(EventContext("b", chain.predicate)).get()
        assertEquals("b<HALT>", actual2.event)
        val expected2 = mapOf<Any, Any>("B1" to "B1", "A1" to "A1", "A0" to "A0")
        assertEquals(expected2, actual2.attributes)
        // Filter not matched
        val actual3 = chain.process(EventContext("c", chain.predicate)).get()
        assertEquals("c", actual3.event)
        val expected3 = mapOf<Any, Any>("B1" to "B1", "B2" to "B2", "A1" to "A1", "A0" to "A0")
        assertEquals(expected3, actual3.attributes)

        // Nested chain unmatched
        val actual4 = chain.process(EventContext("d", chain.predicate)).get()
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
