package co.there4.hexagon.messaging

import co.there4.hexagon.messaging.RabbitClient.Companion.createConnectionFactory
import org.testng.annotations.Test
import kotlin.test.assertFailsWith

@Test class RabbitClientTest {
    fun create_a_connection_factory_with_empty_URI_fails () {
        assertFailsWith(IllegalArgumentException::class) {
            createConnectionFactory("")
        }
    }

    fun create_a_connection_factory_with_invalid_URI_fails() {
        assertFailsWith(IllegalArgumentException::class) {
            createConnectionFactory("http://localhost")
        }
    }

    fun create_a_connection_factory_without_parameters_succeed() {
        val uri = "amqp://user:pass@localhost:12345"
        val cf = createConnectionFactory(uri)
        assert (cf.host == "localhost")
        assert (cf.port == 12345)
    }

    fun create_a_connection_factory_with_one_parameter_succeed() {
        val uri = "amqp://user:pass@localhost:12345?channelCacheSize=50"
        val cf = createConnectionFactory(uri)
        assert (cf.host == "localhost")
        assert (cf.port == 12345)
    }

    fun create_a_connection_factory_with_two_parameter_succeed() {
        val uri = "amqp://user:pass@localhost:12345?channelCacheSize=50&heartbeat=25"
        val cf = createConnectionFactory(uri)
        assert (cf.host == "localhost")
        assert (cf.port == 12345)
    }

    fun create_a_connection_factory_with_all_parameters_succeed() {
        val opt = "channelCacheSize=50&heartbeat=25&automaticRecovery=true&recoveryInterval=5"
        val uri = "amqp://user:pass@localhost:12345?$opt"
        val cf = createConnectionFactory(uri)
        assert (cf.host == "localhost")
        assert (cf.port == 12345)
    }

    @Test fun rabbit_client_disconnects_properly () {
        val client = RabbitClient("amqp://guest:guest@localhost")
        assert (client.connected)
        client.close()
        assert (!client.connected)
        assertFailsWith<IllegalStateException> {
            client.close()
        }
    }
}
