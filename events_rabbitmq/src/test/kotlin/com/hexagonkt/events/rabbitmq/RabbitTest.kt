package com.hexagonkt.events.rabbitmq

import com.hexagonkt.events.Event
import com.hexagonkt.serialization.serialize
import org.testng.annotations.AfterClass
import org.testng.annotations.BeforeClass
import org.testng.annotations.Test
import java.lang.System.currentTimeMillis
import java.net.URI

@Test class RabbitTest {
    data class Sample(val str: String, val int: Int) : Event()

    private companion object {
        private const val URI = "amqp://guest:guest@localhost"
        private const val QUEUE = "test"
        private const val QUEUE_ERROR = "error"
        private const val SUFFIX = "DONE"
        private const val DELAY = 10L
    }

    private val consumer: RabbitMqClient = RabbitMqClient(URI(URI))
    private val client: RabbitMqClient = RabbitMqClient(URI(URI))

    @BeforeClass fun startConsumer() {
        consumer.declareQueue(QUEUE)
        consumer.consume(QUEUE, String::class) { a ->
            Thread.sleep(DELAY)
            a + SUFFIX
        }

        consumer.declareQueue(QUEUE_ERROR)
        consumer.consume(QUEUE_ERROR, String::class) { a ->
            throw RuntimeException("Error with: $a")
        }
    }

    @AfterClass fun deleteTestQueue() {
        consumer.deleteQueue(QUEUE)
        consumer.deleteQueue(QUEUE_ERROR)
        consumer.close()
    }

    fun `call return expected results` () {
        val ts = currentTimeMillis().toString()
        assert(client.call(QUEUE, ts) == ts + SUFFIX)
        val result = client.call(QUEUE_ERROR, ts)
        assert(result.contains(ts) && result.contains("Error with: $ts"))
    }

    // TODO Test call errors
    @Test(enabled = false) fun `call errors` () {
        consumer.consume("aq", Sample::class) {
            if (it.str == "no message error")
                throw IllegalStateException()
            if (it.str == "message error")
                error("message")
        }

        client.publish("aq", Sample("foo", 1).serialize())
        client.call("aq", Sample("no message error", 1).serialize())
        client.call("aq", Sample("message error", 1).serialize())
    }
}
