package co.there4.hexagon.messaging

import org.testng.annotations.AfterClass
import org.testng.annotations.BeforeClass
import org.testng.annotations.Test
import java.lang.System.currentTimeMillis

@Test class RabbitDemo {
    private val URI = "amqp://guest:guest@localhost"
    private val QUEUE = "test"
    private val SUFFIX = "DONE"
    private val DELAY = 10L

    private var consumer: RabbitClient? = null
    private var client: RabbitClient? = null

    @BeforeClass fun startConsumer () {
        consumer = RabbitClient (URI)
        consumer?.declareQueue (QUEUE)
        consumer?.reply (QUEUE, String::class) { a ->
            Thread.sleep (DELAY)
            a + SUFFIX
        }

        client = RabbitClient (URI)
    }

    @AfterClass fun deleteTestQueue () {
        consumer?.deleteQueue (QUEUE)
    }

    @Test (threadPoolSize = 8, invocationCount = 50)
    fun call_return_expected_results () {
        val ts = currentTimeMillis ().toString ()
        assert (client?.call (QUEUE, ts).equals (ts + SUFFIX))
    }
}
