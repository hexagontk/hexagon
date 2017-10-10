package com.hexagonkt.events.rabbitmq

import com.hexagonkt.helpers.CachedLogger
import org.testng.annotations.AfterClass
import org.testng.annotations.BeforeClass
import org.testng.annotations.Test
import java.lang.System.currentTimeMillis
import java.net.URI

@Test class RabbitConnectionTest {
    private companion object : CachedLogger(RabbitConnectionTest::class) {
        private const val port = 5673
        private const val user = "guest"
        private const val password = "guest"
        private const val vhost = "test"

        private const val URI = "amqp://$user:$password@localhost:$port/$vhost"
        private const val QUEUE = "test"
        private const val QUEUE_ERROR = "error"
        private const val SUFFIX = "DONE"
        private const val DELAY = 10L
    }

    private val broker = EmbeddedAMQPBroker(port, user, password, vhost)

    private val consumer: RabbitMqClient by lazy { RabbitMqClient(URI(URI)) }
    private val client: RabbitMqClient by lazy { RabbitMqClient(URI(URI)) }

    @BeforeClass fun startConsumer() {
        broker.startup()

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

        broker.shutdown()
    }

    fun `call return expected results` () {
        val ts = currentTimeMillis().toString()
        assert(client.call(QUEUE, ts) == ts + SUFFIX)
        val result = client.call(QUEUE_ERROR, ts)
        assert(result.contains(ts) && result.contains("Error with: $ts"))

        broker.shutdown()
        try {
            client.call(QUEUE_ERROR, ts)
        }
        catch (e: Exception) {
            error("Consumer error", e)
        }

        startConsumer()
        val ts2 = currentTimeMillis().toString()
        assert(client.call(QUEUE, ts2) == ts2 + SUFFIX)
        val result2 = client.call(QUEUE_ERROR, ts2)
        assert(result2.contains(ts2) && result2.contains("Error with: $ts2"))
    }
}
