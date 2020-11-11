package com.hexagonkt.messaging.rabbitmq

import com.hexagonkt.logging.Logger

import org.junit.jupiter.api.AfterAll
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestInstance
import org.junit.jupiter.api.TestInstance.Lifecycle.PER_CLASS
import java.lang.System.currentTimeMillis
import java.net.URI

@TestInstance(PER_CLASS)
class RabbitConnectionTest {

    private companion object {
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

    private val log: Logger = Logger(this::class)

    private val broker = EmbeddedAMQPBroker(port, user, password, vhost)

    private val consumer: RabbitMqClient by lazy { RabbitMqClient(URI(URI)) }
    private val client: RabbitMqClient by lazy { RabbitMqClient(URI(URI)) }

    @BeforeAll fun startConsumer() {
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

    @AfterAll fun deleteTestQueue() {
        consumer.deleteQueue(QUEUE)
        consumer.deleteQueue(QUEUE_ERROR)
        consumer.close()

        broker.shutdown()
    }

    @Test fun `call return expected results`() {
        val ts = currentTimeMillis().toString()
        assert(client.call(QUEUE, ts) == ts + SUFFIX)
        val result = client.call(QUEUE_ERROR, ts)
        assert(result.contains(ts) && result.contains("Error with: $ts"))

        broker.shutdown()
        try {
            client.call(QUEUE_ERROR, ts)
        }
        catch (e: Exception) {
            log.error(e) { "Consumer error" }
        }

        startConsumer()
        val ts2 = currentTimeMillis().toString()
        assert(client.call(QUEUE, ts2) == ts2 + SUFFIX)
        val result2 = client.call(QUEUE_ERROR, ts2)
        assert(result2.contains(ts2) && result2.contains("Error with: $ts2"))
    }
}
