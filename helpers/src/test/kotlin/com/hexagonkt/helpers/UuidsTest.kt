package com.hexagonkt.helpers

import kotlin.test.Test
import java.util.UUID

internal class UuidsTest {

    @Test fun `UUID can be serialized and parsed to base 64`() {
        val uuid = UUID.randomUUID()
        val str = uuid.toString()
        val b64 = uuid.toBase64()

        assert(uuid(b64) == uuid(str))
    }
}
