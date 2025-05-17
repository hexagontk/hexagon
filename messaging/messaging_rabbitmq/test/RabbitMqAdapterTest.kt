package com.hexagontk.messaging.rabbitmq

import com.hexagontk.core.requireInt
import com.hexagontk.core.requireString
import com.hexagontk.messaging.MessagingPort
import com.hexagontk.messaging.rabbitmq.RabbitTest.Companion.PORT
import com.hexagontk.messaging.rabbitmq.RabbitTest.Sample
import com.hexagontk.serialization.jackson.json.Json
import org.junit.jupiter.api.condition.DisabledOnOs
import org.junit.jupiter.api.condition.OS
import kotlin.test.Test

@DisabledOnOs(OS.WINDOWS)
internal class RabbitMqAdapterTest {
    /**
     * TODO Add asserts
     */
    @Test fun `Event manager` () {
        val engine: MessagingPort = RabbitMqAdapter("amqp://guest:guest@localhost:$PORT", Json)
        val decoder = { it: Map<String, *> ->
            Sample(it.requireString(Sample::str), it.requireInt(Sample::int))
        }
        engine.consume(Sample::class, decoder) {
            if (it.str == "no message error")
                throw IllegalStateException()
            if (it.str == "message error")
                error("message")
        }

        engine.publish(Sample("foo", 1))
//        EventManager.publish(Sample("no message error", 1))
//        EventManager.publish(Sample("message error", 1))
    }
}
