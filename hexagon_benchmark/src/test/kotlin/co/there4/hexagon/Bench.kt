
package co.there4.hexagon

import org.openjdk.jmh.annotations.Benchmark

open class Bench {
    @Benchmark
    fun foo() {
        println("bench")
        Thread.sleep(100L)
    }

    @Benchmark
    fun bar() {
        println("bench")
        Thread.sleep(100L)
    }
}
