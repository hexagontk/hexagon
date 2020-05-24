package com.hexagonkt.messaging.rabbitmq

import com.hexagonkt.messaging.MessagingPort
import org.junit.jupiter.api.Test

class RabbitMqAdapterTest {
    /**
     * TODO Add asserts
     */
    @Test fun `Event manager` () {
        val engine: MessagingPort = RabbitMqAdapter()
        engine.consume(com.hexagonkt.messaging.rabbitmq.RabbitTest.Sample::class) {
            if (it.str == "no message error")
                throw IllegalStateException()
            if (it.str == "message error")
                error("message")
        }
        engine.publish(com.hexagonkt.messaging.rabbitmq.RabbitTest.Sample("foo", 1))
//        EventManager.publish(Sample("no message error", 1))
//        EventManager.publish(Sample("message error", 1))
    }
}
