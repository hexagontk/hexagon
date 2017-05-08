package co.there4.hexagon.events.rabbitmq

import co.there4.hexagon.helpers.CachedLogger
import org.testng.annotations.AfterClass
import org.testng.annotations.BeforeClass
import org.testng.annotations.Test
import java.lang.System.currentTimeMillis

@Test class RabbitConnectionTest {
    companion object : CachedLogger(RabbitConnectionTest::class)

    private val broker = EmbeddedAMQPBroker()

    private val URI = "amqp://guest:guest@localhost:5673/test"
    private val QUEUE = "test"
    private val QUEUE_ERROR = "error"
    private val SUFFIX = "DONE"
    private val DELAY = 10L

    private var consumer: RabbitClient? = null
    private var client: RabbitClient? = null

    @BeforeClass fun startConsumer () {
        broker.startup()

        consumer = RabbitClient (URI)
        consumer?.declareQueue (QUEUE)
        consumer?.consume (QUEUE, String::class) { a ->
            Thread.sleep (DELAY)
            a + SUFFIX
        }

        consumer?.declareQueue (QUEUE_ERROR)
        consumer?.consume (QUEUE_ERROR, String::class) { a ->
            throw RuntimeException("Error with: $a")
        }

        client = RabbitClient (URI)
    }

    @AfterClass fun deleteTestQueue () {
        consumer?.deleteQueue (QUEUE)
        consumer?.deleteQueue (QUEUE_ERROR)
        consumer?.close()

        broker.shutdown()
    }

    fun call_return_expected_results () {
        val ts = currentTimeMillis ().toString ()
        assert (client?.call (QUEUE, ts).equals (ts + SUFFIX))
        val result = client?.call (QUEUE_ERROR, ts) ?: ""
        assert (result.contains (ts) && result.contains ("Error with: $ts"))

        broker.shutdown()
        try {
            client?.call (QUEUE_ERROR, ts)
        }
        catch (e: Exception) {
            error("Consumer error", e)
        }

        startConsumer()
        val ts2 = currentTimeMillis ().toString ()
        assert (client?.call (QUEUE, ts2).equals (ts2 + SUFFIX))
        val result2 = client?.call (QUEUE_ERROR, ts2) ?: ""
        assert (result2.contains (ts2) && result2.contains ("Error with: $ts2"))
    }
}
