package com.hexagontk.messaging

import java.time.LocalDateTime
import kotlin.test.Test

internal class MessageTest {

    @Test fun `Default message load environment properties`() {
        val message = Message()

        assert(message.timestamp > 0)
        assert(message.dateTime.isAfter(LocalDateTime.now().minusDays(1)))
        assert(message.hostname.isNotBlank())
        assert(message.ip.isNotBlank())
//        assert(message.jvmId.isNotBlank())
        assert(message.thread.isNotBlank())
    }
}
