package com.hexagonkt.events.rabbitmq

import com.hexagonkt.events.Event
import com.hexagonkt.events.EventManager
import org.testng.annotations.Test

@Test class RabbitMqEventEngineTest {
    data class Sample(val str: String, val int: Int) : Event()

    /**
     * TODO Add asserts
     */
    fun event_manager() {
        EventManager.engine = com.hexagonkt.events.rabbitmq.RabbitMqEventEngine()
        EventManager.consume(com.hexagonkt.events.rabbitmq.RabbitMqEventEngineTest.Sample::class) {
            if (it.str == "no message error")
                throw IllegalStateException()
            if (it.str == "message error")
                error("message")
        }
        EventManager.publish(com.hexagonkt.events.rabbitmq.RabbitMqEventEngineTest.Sample("foo", 1))
//        EventManager.publish(Sample("no message error", 1))
//        EventManager.publish(Sample("message error", 1))
    }
}
