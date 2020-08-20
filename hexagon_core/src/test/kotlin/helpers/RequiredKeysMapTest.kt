package com.hexagonkt.helpers

import org.junit.jupiter.api.Test
import java.lang.IllegalStateException
import kotlin.test.assertFailsWith

class RequiredKeysMapTest {

    private val map = RequiredKeysMap(mapOf("a" to "b"))

    @Test fun `Values are accessed correctly`() {
        assert(map["a"] == "b")
    }

    @Test fun `Missing keys raise an exception instead returning 'null'`() {
        assertFailsWith<IllegalStateException> {
            map["b"]
        }
    }
}
