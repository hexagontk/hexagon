package com.hexagonkt.messaging.rabbitmq

import com.hexagonkt.messaging.EventsPort
import org.testng.annotations.Test

@Test class RabbitMqAdapterTest {
    /**
     * TODO Add asserts
     */
    fun `event manager` () {
        val engine: EventsPort = RabbitMqAdapter()
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
