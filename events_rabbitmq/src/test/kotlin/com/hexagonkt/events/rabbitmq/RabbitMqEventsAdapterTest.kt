package com.hexagonkt.events.rabbitmq

import com.hexagonkt.events.EventManager
import org.testng.annotations.Test

@Test class RabbitMqEventsAdapterTest {
    /**
     * TODO Add asserts
     */
    fun `event manager` () {
        EventManager.engine = com.hexagonkt.events.rabbitmq.RabbitMqEventsAdapter()
        EventManager.consume(com.hexagonkt.events.rabbitmq.RabbitTest.Sample::class) {
            if (it.str == "no message error")
                throw IllegalStateException()
            if (it.str == "message error")
                error("message")
        }
        EventManager.publish(com.hexagonkt.events.rabbitmq.RabbitTest.Sample("foo", 1))
//        EventManager.publish(Sample("no message error", 1))
//        EventManager.publish(Sample("message error", 1))
    }
}
