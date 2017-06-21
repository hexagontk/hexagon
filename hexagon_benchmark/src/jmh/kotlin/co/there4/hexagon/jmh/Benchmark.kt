
package co.there4.hexagon.jmh

import org.openjdk.jmh.annotations.Benchmark

open class Benchmark {
    @Benchmark
    fun foo() {
        println("foo bench")
        Thread.sleep(100L)
    }

    @Benchmark
    fun bar() {
        println("bar bench")
        Thread.sleep(100L)
    }
}
