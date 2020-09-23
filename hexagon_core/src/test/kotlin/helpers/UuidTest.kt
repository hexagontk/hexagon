package com.hexagonkt.helpers

import org.junit.jupiter.api.Test
import java.util.*

class UuidTest {

    @Test fun `UUID can be serialized and parsed to base 64`() {
        val uuid = UUID.randomUUID()
        val str = uuid.toString()
        val b64 = uuid.toBase64()

        assert(uuid(b64) == uuid(str))
    }
}
