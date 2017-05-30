package co.there4.hexagon.events.rabbitmq

import org.testng.annotations.AfterClass
import org.testng.annotations.BeforeClass
import org.testng.annotations.Test
import java.lang.System.currentTimeMillis
import java.net.URI

@Test class RabbitTest {
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

    fun call_return_expected_results() {
        val ts = currentTimeMillis().toString()
        assert(client.call(QUEUE, ts) == ts + SUFFIX)
        val result = client.call(QUEUE_ERROR, ts)
        assert(result.contains(ts) && result.contains("Error with: $ts"))
    }
}
