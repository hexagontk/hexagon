package com.hexagontk.handlers

import org.openjdk.jmh.annotations.Benchmark
import org.openjdk.jmh.annotations.BenchmarkMode
import org.openjdk.jmh.annotations.Fork
import org.openjdk.jmh.annotations.Measurement
import org.openjdk.jmh.annotations.Mode
import org.openjdk.jmh.annotations.OperationsPerInvocation
import org.openjdk.jmh.annotations.OutputTimeUnit
import org.openjdk.jmh.annotations.Scope
import org.openjdk.jmh.annotations.State
import org.openjdk.jmh.annotations.Warmup
import org.openjdk.jmh.infra.Blackhole
import java.util.concurrent.TimeUnit

@BenchmarkMode(Mode.Throughput)
@Fork(1)
@Measurement(iterations = 10, batchSize = 1, time = 1, timeUnit = TimeUnit.SECONDS)
@OutputTimeUnit(TimeUnit.MILLISECONDS)
@OperationsPerInvocation(5)
@Warmup(time = 1, timeUnit = TimeUnit.SECONDS, batchSize = 5, iterations = 1)
@State(Scope.Benchmark)
open class JmhBenchmark {

    class EventContext<T : Any>(
        override var event: T,
        override var predicate: (Context<T>) -> Boolean,
        override var nextHandlers: Array<Handler<T>> = emptyArray(),
        override var nextHandler: Int = 0,
        override var exception: Exception? = null,
        override var attributes: Map<*, *> = emptyMap<Any, Any>(),
        override var handled: Boolean = false,
    ) : Context<T> {

        override fun with(
            event: T,
            predicate: (Context<T>) -> Boolean,
            nextHandlers: Array<Handler<T>>,
            nextHandler: Int,
            exception: Exception?,
            attributes: Map<*, *>,
            handled: Boolean,
        ): EventContext<T> =
            apply {
                this.event = event
                this.predicate = predicate
                this.nextHandlers = nextHandlers
                this.nextHandler = nextHandler
                this.exception = exception
                this.attributes = attributes
                this.handled = handled
            }
    }

    private fun <T : Any> Handler<T>.process(event: T): T =
        process(EventContext(event, predicate)).event

    private val chains = listOf(
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

    private val filtersChain = ChainHandler<String>(
        AfterHandler { it.appendText("<A1>") },
        OnHandler { it.appendText("<B1>")},
        FilterHandler({ it.hasLetters('a', 'b') }) {
            if (it.event.startsWith("a"))
                it.with(event = it.event + "<PASS>").next()
            else
                it.with(event = it.event + "<HALT>")
        },
        OnHandler { it.appendText("<B2>")},
    )

    @Benchmark fun beforeAndAfterHandlersAreCalledInOrder(bh: Blackhole) {

        // No handler called
        chains.forEach {
            bh.consume(it.process(""))
            bh.consume(it.process("_"))
        }

        // All handlers called
        chains.forEach {
            bh.consume(it.process("abcd"))
            bh.consume(it.process("dcba"))
            bh.consume(it.process("afch"))
            bh.consume(it.process("hcfa"))
            bh.consume(it.process("hcfa_"))
        }

        // Single handler called
        chains.forEach {
            bh.consume(it.process("a"))
            bh.consume(it.process("e"))
            bh.consume(it.process("ae"))
            bh.consume(it.process("a_"))

            bh.consume(it.process("b"))
            bh.consume(it.process("f"))
            bh.consume(it.process("bf"))
            bh.consume(it.process("b_"))

            bh.consume(it.process("c"))
            bh.consume(it.process("g"))
            bh.consume(it.process("cg"))
            bh.consume(it.process("c_"))

            bh.consume(it.process("d"))
            bh.consume(it.process("h"))
            bh.consume(it.process("dh"))
            bh.consume(it.process("d_"))
        }

        // Two handlers called
        chains.forEach {
            bh.consume(it.process("af"))
            bh.consume(it.process("bg"))
            bh.consume(it.process("ch"))
            bh.consume(it.process("de"))
        }
    }

    @Benchmark fun filtersAllowPassingAndHalting(bh: Blackhole) {
        // Filter passing
        bh.consume(filtersChain.process("a"))
        // Filter halting
        bh.consume(filtersChain.process("b"))
        // Filter not matched
        bh.consume(filtersChain.process("c"))
    }

    private fun Context<String>.hasLetters(vararg letters: Char): Boolean =
        letters.any { event.contains(it) }

    private fun Context<String>.appendText(text: String): Context<String> =
        with(event = event + text)
}
