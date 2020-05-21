package com.hexagonkt.helpers

import org.testng.annotations.Test
import java.lang.IllegalStateException

class RequiredKeysMapTest {

    val map = RequiredKeysMap(mapOf("a" to "b"))

    @Test fun `Values are accessed correctly`() {
        assert(map["a"] == "b")
    }

    @Test(expectedExceptions = [ IllegalStateException::class ])
    fun `Missing keys raise an exception instead returning 'null'`() {
        map["b"]
    }
}
