package com.hexagonkt.messaging.rabbitmq

import com.hexagonkt.messaging.MessagingPort
import com.hexagonkt.messaging.rabbitmq.RabbitTest.Companion.PORT
import com.hexagonkt.serialization.JacksonMapper
import com.hexagonkt.serialization.Json
import com.hexagonkt.serialization.SerializationManager
import org.junit.jupiter.api.Test

class RabbitMqAdapterTest {
    /**
     * TODO Add asserts
     */
    @Test fun `Event manager` () {
        val engine: MessagingPort = RabbitMqAdapter("amqp://guest:guest@localhost:$PORT")
        engine.consume(RabbitTest.Sample::class) {
            if (it.str == "no message error")
                throw IllegalStateException()
            if (it.str == "message error")
                error("message")
        }

        SerializationManager.mapper = JacksonMapper
        SerializationManager.formats = linkedSetOf(Json)
        engine.publish(RabbitTest.Sample("foo", 1))
//        EventManager.publish(Sample("no message error", 1))
//        EventManager.publish(Sample("message error", 1))
    }
}
