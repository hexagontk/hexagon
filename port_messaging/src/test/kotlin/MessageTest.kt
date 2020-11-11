package com.hexagonkt.messaging

import org.junit.jupiter.api.Test

internal class MessageTest {

    @Test fun `Default message load environment properties`() {
        val message = Message()

        assert(message.timestamp > 0)
        assert(message.dateTime > 0)
        assert(message.hostname.isNotBlank())
        assert(message.ip.isNotBlank())
        assert(message.jvmId.isNotBlank())
        assert(message.thread.isNotBlank())
    }
}
