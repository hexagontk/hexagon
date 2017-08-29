
package com.hexagonkt.jmh

import org.openjdk.jmh.annotations.Benchmark

/**
 * For this benchmark to compile inside IntelliJ, src/jmh/kotlin should be configured as a test
 * source set.
 */
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
