package com.hexagonkt.core.handlers

import com.hexagonkt.core.helpers.fail
import com.hexagonkt.core.logging.LoggingLevel.OFF
import com.hexagonkt.core.logging.LoggingLevel.TRACE
import com.hexagonkt.core.logging.LoggingManager
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.delay
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.*
import org.junit.jupiter.api.TestInstance.Lifecycle.PER_CLASS
import java.lang.IllegalStateException
import kotlin.system.measureNanoTime
import kotlin.test.assertEquals
import kotlin.test.assertIs
import kotlin.test.assertTrue

/*
 * IMPORTANT: Using `runBlockingTest` skip test delays (be aware if timing seems odd)
 */
@ExperimentalCoroutinesApi
@TestInstance(PER_CLASS)
internal class ChainHandlerTest {

    private val testChain = ChainHandler(
        AfterHandler { println("last"); it },
        AfterHandler { println("last2"); it },
        OnHandler { println("1"); delay(40); it },
        OnHandler({ false }) { println("2"); it },
        ChainHandler(
            FilterHandler {
                println("3")
                delay(20)
                val n = it.next()
                println("3")
                n
            },
            OnHandler { println("4"); it },
        )
    )

    @BeforeAll fun enableLogging() {
        LoggingManager.setLoggerLevel(OFF)
        LoggingManager.setLoggerLevel("com.hexagonkt.core", TRACE)
    }

    @AfterAll fun disableLogging() {
        LoggingManager.setLoggerLevel(OFF)
    }

    @Test fun `Build a nested chain of handlers`() = runTest {
        var flags = listOf(true, true, true, true)

        val chain = ChainHandler(
            OnHandler({ flags[0] }) { it.copy(event = "a") },
            OnHandler({ flags[1] }) { it.copy(event = "b") },
            OnHandler({ flags[2] }) { it.copy(event = "c") },
            OnHandler({ flags[3] }) { it.copy(event = "d") },
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

    @Test fun `Chains of handlers pass the correct handler to context`() = runTest {

        val filterAE: suspend (Context<String>) -> Boolean = { it.hasLetters('a', 'e') }
        val filterBF: suspend (Context<String>) -> Boolean = { it.hasLetters('b', 'f') }
        val filterCG: suspend (Context<String>) -> Boolean = { it.hasLetters('c', 'g') }
        val filterDH: suspend (Context<String>) -> Boolean = { it.hasLetters('d', 'h') }

        val chainHandler =
            ChainHandler(
                AfterHandler(filterAE) {
                    assertEquals(filterAE, it.currentFilter)
                    it.appendText("<A1>")
                },
                AfterHandler(filterBF) {
                    assertEquals(filterBF, it.currentFilter)
                    it.appendText("<A2>")
                },
                OnHandler(filterCG) {
                    assertEquals(filterCG, it.currentFilter)
                    it.appendText("<B1>")
                },
                OnHandler(filterDH) {
                    assertEquals(filterDH, it.currentFilter)
                    it.appendText("<B2>")
                },
            )

        assertEquals("abcdefgh<B1><B2><A2><A1>", chainHandler.process("abcdefgh"))
    }

    @Test fun `Build a chain of handlers`() = runTest {

        fun createChainHandler() =
            ChainHandler(
                AfterHandler { it },
                OnHandler { it },
                ChainHandler(
                    FilterHandler { it.next() },
                    OnHandler { it },
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

    @Test fun `Process event with chain of handlers ('delay' ignored)`() = runTest {
        val t = time(5) {
            assertEquals("/abc", testChain.process("/abc"))
        }

        assertTrue(t < 10.0)
    }

    @Test fun `Handlers run coroutines properly (verified with 'delay')`() = runBlocking {
        val t = time(5) {
            assertEquals("/abc", testChain.process("/abc"))
        }

        assert(t in 60.0..200.0) // Range is high because of CI execution times
    }

    @Test fun `Before and after handlers are called in order`() = runTest {

        val chains = listOf(
            ChainHandler<String>(
                AfterHandler({ it.hasLetters('a', 'e') }) { it.appendText("<A1>") },
                AfterHandler({ it.hasLetters('b', 'f') }) { it.appendText("<A2>") },
                OnHandler({ it.hasLetters('c', 'g') }) { it.appendText("<B1>") },
                OnHandler({ it.hasLetters('d', 'h') }) { it.appendText("<B2>") },
            ),
            ChainHandler<String>(
                OnHandler({ it.hasLetters('c', 'g') }) { it.appendText("<B1>") },
                AfterHandler({ it.hasLetters('a', 'e') }) { it.appendText("<A1>") },
                OnHandler({ it.hasLetters('d', 'h') }) { it.appendText("<B2>") },
                AfterHandler({ it.hasLetters('b', 'f') }) { it.appendText("<A2>") },
            ),
            ChainHandler<String>(
                OnHandler({ it.hasLetters('c', 'g') }) { it.appendText("<B1>") },
                OnHandler({ it.hasLetters('d', 'h') }) { it.appendText("<B2>") },
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

    @Test fun `Filters allow passing and halting`() = runTest {
        val chain = ChainHandler<String>(
            AfterHandler { it.appendText("<A1>") },
            OnHandler { it.appendText("<B1>")},
            FilterHandler({ it.hasLetters('a', 'b') }) {
                if (it.event.startsWith("a"))
                    it.copy(event = it.event + "<PASS>").next()
                else
                    it.copy(event = it.event + "<HALT>")
            },
            OnHandler { it.appendText("<B2>")},
        )

        // Filter passing
        assertEquals("a<B1><PASS><B2><A1>", chain.process("a"))
        // Filter halting
        assertEquals("b<B1><HALT><A1>", chain.process("b"))
        // Filter not matched
        assertEquals("c<B1><B2><A1>", chain.process("c"))
    }

    @Test fun `Chained handlers are executed as blocks`() = runTest {
        val chain = ChainHandler<String>(
            OnHandler { it.appendText("<B0>") },
            AfterHandler { it.appendText("<A0>") },
            ChainHandler({ it.hasLetters('a', 'b', 'c') },
                OnHandler { it.appendText("<B1>")},
                AfterHandler { it.appendText("<A1>") },
                FilterHandler({ it.hasLetters('a', 'b') }) {
                    if (it.event.startsWith("a"))
                        it.copy(event = it.event + "<PASS>").next()
                    else
                        it.copy(event = it.event + "<HALT>")
                },
                OnHandler { it.appendText("<B2>")},
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

    @Test fun `Many chained handlers are processed properly`() = runTest {
        val chain = ChainHandler<String>(
            OnHandler { it.appendText("<B0>") },
            ChainHandler({ it.hasLetters('a', 'b') },
                OnHandler { it.appendText("<B1>")},
            ),
            ChainHandler({ it.hasLetters('a') },
                OnHandler { it.appendText("<B2>")},
            ),
            ChainHandler({ it.hasLetters('c') },
                OnHandler { it.appendText("<B3>")},
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

    @Test fun `Chained handlers are executed as blocks bug`() = runTest {
        val chain = ChainHandler<String>(
            OnHandler { it.appendText("<B0>") },
            ChainHandler({ it.hasLetters('a', 'b', 'c') },
                OnHandler { it.appendText("<B1>")},
                FilterHandler({ it.hasLetters('a', 'b') }) {
                    if (it.event.startsWith("a"))
                        it.copy(event = it.event + "<PASS>").next()
                    else
                        it.copy(event = it.event + "<HALT>")
                },
                OnHandler { it.appendText("<B2>")},
                OnHandler { it.appendText("<A1>") },
            ),
            OnHandler { it.appendText("<A0>") },
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

    @Test fun `Exceptions don't prevent handlers execution`() = runTest {
        val chain = ChainHandler<String>(
            OnHandler { fail },
            AfterHandler { it.appendText("<A0>") },
            ChainHandler(
                { it.hasLetters('a', 'b', 'c') },
                OnHandler { it.appendText("<B1>") },
                AfterHandler {
                    assertTrue(it.exception is IllegalStateException)
                    it.appendText("<A1>")
                },
                FilterHandler({ it.hasLetters('a', 'b') }) {
                    if (it.event.startsWith("a"))
                        it.copy(event = it.event + "<PASS>").next()
                    else
                        it.copy(event = it.event + "<HALT>")
                },
                OnHandler { it.appendText("<B2>") },
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

    @Test fun `Exceptions don't prevent handlers execution in after`() = runTest {
        val chain1 = ChainHandler<String>(
            AfterHandler { fail },
            AfterHandler { it.appendText("<A0>") },
        )

        assertEquals("a<A0>", chain1.process("a"))

        val chain2 = ChainHandler<String>(
            AfterHandler {
                assertTrue(it.exception is IllegalStateException)
                it.appendText("<A0>")
            },
            AfterHandler { fail },
        )

        assertEquals("a<A0>", chain2.process("a"))
    }

    @Test fun `Exceptions don't prevent handlers execution in filters`() = runTest {
        val chain = ChainHandler<String>(
            FilterHandler { fail },
            OnHandler { it.appendText("<B0>") },
        )

        val actual = chain.process(Context("a", chain.predicate))
        assertIs<IllegalStateException>(actual.exception)
        assertEquals("a", actual.event)
    }

    @Test fun `Context attributes are passed correctly`() = runTest {
        val chain = ChainHandler<String>(
            OnHandler { fail },
            AfterHandler { it.copy(attributes = it.attributes + ("A0" to "A0")) },
            ChainHandler(
                { it.hasLetters('a', 'b', 'c') },
                OnHandler { it.copy(attributes = it.attributes + ("B1" to "B1")) },
                AfterHandler {
                    assertTrue(it.exception is IllegalStateException)
                    it.copy(attributes = it.attributes + ("A1" to "A1"))
                },
                FilterHandler({ it.hasLetters('a', 'b') }) {
                    if (it.event.startsWith("a"))
                        it.copy(event = it.event + "<PASS>").next()
                    else
                        it.copy(event = it.event + "<HALT>")
                },
                OnHandler { it.copy(attributes = it.attributes + ("B2" to "B2")) },
            ),
        )

        // Chained filter
        val actual1 = chain.process(Context("a", chain.predicate))
        assertEquals("a<PASS>", actual1.event)
        val expected1 = mapOf<Any, Any>("B1" to "B1", "B2" to "B2", "A1" to "A1", "A0" to "A0")
        assertEquals(expected1, actual1.attributes)
        // Filter halting
        val actual2 = chain.process(Context("b", chain.predicate))
        assertEquals("b<HALT>", actual2.event)
        val expected2 = mapOf<Any, Any>("B1" to "B1", "A1" to "A1", "A0" to "A0")
        assertEquals(expected2, actual2.attributes)
        // Filter not matched
        val actual3 = chain.process(Context("c", chain.predicate))
        assertEquals("c", actual3.event)
        val expected3 = mapOf<Any, Any>("B1" to "B1", "B2" to "B2", "A1" to "A1", "A0" to "A0")
        assertEquals(expected3, actual3.attributes)

        // Nested chain unmatched
        val actual4 = chain.process(Context("d", chain.predicate))
        assertEquals("d", actual4.event)
        val expected4 = mapOf<Any, Any>("A0" to "A0")
        assertEquals(expected4, actual4.attributes)
    }

    private fun Context<String>.hasLetters(vararg letters: Char): Boolean =
        letters.any { event.contains(it) }

    private fun Context<String>.appendText(text: String): Context<String> =
        copy(event = event + text)

    private suspend fun time(times: Int, block: suspend () -> Unit): Double =
        (0 until times).minOf {
            (measureNanoTime { block() } / 10e5).apply { println(">>> $this") }
        }
}
