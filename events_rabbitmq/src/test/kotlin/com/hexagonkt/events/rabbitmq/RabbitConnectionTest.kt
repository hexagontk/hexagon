package com.hexagonkt.events.rabbitmq

import com.hexagonkt.helpers.CachedLogger
import org.testng.annotations.AfterClass
import org.testng.annotations.BeforeClass
import org.testng.annotations.Test
import java.lang.System.currentTimeMillis
import java.net.URI

@Test class RabbitConnectionTest {
    private companion object : CachedLogger(com.hexagonkt.events.rabbitmq.RabbitConnectionTest::class) {
        private const val port = 5673
        private const val user = "guest"
        private const val password = "guest"
        private const val vhost = "test"

        private const val URI = "amqp://${com.hexagonkt.events.rabbitmq.RabbitConnectionTest.Companion.user}:${com.hexagonkt.events.rabbitmq.RabbitConnectionTest.Companion.password}@localhost:${com.hexagonkt.events.rabbitmq.RabbitConnectionTest.Companion.port}/${com.hexagonkt.events.rabbitmq.RabbitConnectionTest.Companion.vhost}"
        private const val QUEUE = "test"
        private const val QUEUE_ERROR = "error"
        private const val SUFFIX = "DONE"
        private const val DELAY = 10L
    }

    private val broker = com.hexagonkt.events.rabbitmq.EmbeddedAMQPBroker(com.hexagonkt.events.rabbitmq.RabbitConnectionTest.Companion.port, com.hexagonkt.events.rabbitmq.RabbitConnectionTest.Companion.user, com.hexagonkt.events.rabbitmq.RabbitConnectionTest.Companion.password, com.hexagonkt.events.rabbitmq.RabbitConnectionTest.Companion.vhost)

    private val consumer: com.hexagonkt.events.rabbitmq.RabbitMqClient by lazy { com.hexagonkt.events.rabbitmq.RabbitMqClient(URI(com.hexagonkt.events.rabbitmq.RabbitConnectionTest.Companion.URI)) }
    private val client: com.hexagonkt.events.rabbitmq.RabbitMqClient by lazy { com.hexagonkt.events.rabbitmq.RabbitMqClient(URI(com.hexagonkt.events.rabbitmq.RabbitConnectionTest.Companion.URI)) }

    @BeforeClass fun startConsumer() {
        broker.startup()

        consumer.declareQueue(com.hexagonkt.events.rabbitmq.RabbitConnectionTest.Companion.QUEUE)
        consumer.consume(com.hexagonkt.events.rabbitmq.RabbitConnectionTest.Companion.QUEUE, String::class) { a ->
            Thread.sleep(com.hexagonkt.events.rabbitmq.RabbitConnectionTest.Companion.DELAY)
            a + com.hexagonkt.events.rabbitmq.RabbitConnectionTest.Companion.SUFFIX
        }

        consumer.declareQueue(com.hexagonkt.events.rabbitmq.RabbitConnectionTest.Companion.QUEUE_ERROR)
        consumer.consume(com.hexagonkt.events.rabbitmq.RabbitConnectionTest.Companion.QUEUE_ERROR, String::class) { a ->
            throw RuntimeException("Error with: $a")
        }
    }

    @AfterClass fun deleteTestQueue() {
        consumer.deleteQueue(com.hexagonkt.events.rabbitmq.RabbitConnectionTest.Companion.QUEUE)
        consumer.deleteQueue(com.hexagonkt.events.rabbitmq.RabbitConnectionTest.Companion.QUEUE_ERROR)
        consumer.close()

        broker.shutdown()
    }

    fun call_return_expected_results() {
        val ts = currentTimeMillis().toString()
        assert(client.call(com.hexagonkt.events.rabbitmq.RabbitConnectionTest.Companion.QUEUE, ts) == ts + com.hexagonkt.events.rabbitmq.RabbitConnectionTest.Companion.SUFFIX)
        val result = client.call(com.hexagonkt.events.rabbitmq.RabbitConnectionTest.Companion.QUEUE_ERROR, ts)
        assert(result.contains(ts) && result.contains("Error with: $ts"))

        broker.shutdown()
        try {
            client.call(com.hexagonkt.events.rabbitmq.RabbitConnectionTest.Companion.QUEUE_ERROR, ts)
        }
        catch (e: Exception) {
            error("Consumer error", e)
        }

        startConsumer()
        val ts2 = currentTimeMillis().toString()
        assert(client.call(com.hexagonkt.events.rabbitmq.RabbitConnectionTest.Companion.QUEUE, ts2) == ts2 + com.hexagonkt.events.rabbitmq.RabbitConnectionTest.Companion.SUFFIX)
        val result2 = client.call(com.hexagonkt.events.rabbitmq.RabbitConnectionTest.Companion.QUEUE_ERROR, ts2)
        assert(result2.contains(ts2) && result2.contains("Error with: $ts2"))
    }
}
