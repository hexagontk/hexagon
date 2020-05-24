package com.hexagonkt.helpers

import io.kotest.assertions.throwables.shouldThrow
import org.junit.jupiter.api.Test
import java.lang.IllegalStateException

class RequiredKeysMapTest {

    private val map = RequiredKeysMap(mapOf("a" to "b"))

    @Test fun `Values are accessed correctly`() {
        assert(map["a"] == "b")
    }

    @Test fun `Missing keys raise an exception instead returning 'null'`() {
        shouldThrow<IllegalStateException> {
            map["b"]
        }
    }
}
