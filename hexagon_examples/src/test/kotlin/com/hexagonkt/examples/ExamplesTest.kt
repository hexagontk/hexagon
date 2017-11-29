package com.hexagonkt.examples

import org.testng.annotations.Test
import java.net.URL


@Test class ExamplesTest {
    fun `Si uso 'to' creo un 'par'` () {
        val pair = "a" to 1
        @Suppress("USELESS_IS_CHECK") assert(pair is Pair<String, Int>)
        assert(pair.first == "a") // == es como equals
        assert(pair.first === "a") // === es como ==
        val u1 = URL("http://example.com")
        val u2 = URL("http://example.com")
        assert(u1 == u2)
        assert(u1 !== u2)
    }
}

@Test class Ex2 {
    fun foo() {
    }
}
