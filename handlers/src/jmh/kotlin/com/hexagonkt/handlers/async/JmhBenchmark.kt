package com.hexagonkt.handlers.async

import com.hexagonkt.handlers.async.*
import org.openjdk.jmh.annotations.Benchmark
import org.openjdk.jmh.annotations.Scope
import org.openjdk.jmh.annotations.State
import org.openjdk.jmh.infra.Blackhole
import java.util.concurrent.CompletableFuture

@State(Scope.Benchmark)
open class JmhBenchmark {

    data class EventContext<T : Any>(
        override val event: T,
        override val predicate: (Context<T>) -> Boolean,
        override val nextHandlers: List<Handler<T>> = emptyList(),
        override val nextHandler: Int = 0,
        override val exception: Exception? = null,
        override val attributes: Map<*, *> = emptyMap<Any, Any>(),
    ) : Context<T> {

        override fun with(
            event: T,
            predicate: (Context<T>) -> Boolean,
            nextHandlers: List<Handler<T>>,
            nextHandler: Int,
            exception: Exception?,
            attributes: Map<*, *>,
        ): EventContext<T> =
            copy(
                event = event,
                predicate = predicate,
                nextHandlers = nextHandlers,
                nextHandler = nextHandler,
                exception = exception,
                attributes = attributes,
            )
    }

    private fun <T : Any> Handler<T>.process(event: T): CompletableFuture<T> =
        process(EventContext(event, predicate)).thenApply(Context<T>::event)

    private val chains = listOf(
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

    private val filtersChain = ChainHandler<String>(
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
