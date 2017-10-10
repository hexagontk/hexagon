package com.hexagonkt.events.rabbitmq

import com.hexagonkt.events.rabbitmq.RabbitMqClient.Companion.createConnectionFactory
import com.hexagonkt.helpers.CachedLogger
import com.hexagonkt.serialization.serialize
import org.testng.annotations.Test
import java.net.URI
import kotlin.test.assertFailsWith

@Test class RabbitMqClientTest {
    companion object : CachedLogger(RabbitMqClientTest::class)

    fun `create a connection factory with empty URI fails` () {
        assertFailsWith(IllegalArgumentException::class) {
            createConnectionFactory(URI(""))
        }
    }

    fun `create a connection factory with invalid URI fails` () {
        assertFailsWith(IllegalArgumentException::class) {
            createConnectionFactory(URI("http://localhost"))
        }
    }

    fun `create a connection factory without parameters succeed` () {
        val uri = "amqp://user:pass@localhost:12345"
        val cf = createConnectionFactory(URI(uri))
        assert(cf.host == "localhost")
        assert(cf.port == 12345)
    }

    fun `create a connection factory with one parameter succeed` () {
        val uri = "amqp://user:pass@localhost:12345?channelCacheSize=50"
        val cf = createConnectionFactory(URI(uri))
        assert(cf.host == "localhost")
        assert(cf.port == 12345)
    }

    fun `create a connection factory with two parameter succeed` () {
        val uri = "amqp://user:pass@localhost:12345?channelCacheSize=50&heartbeat=25"
        val cf = createConnectionFactory(URI(uri))
        assert(cf.host == "localhost")
        assert(cf.port == 12345)
    }

    fun `create a connection factory with all parameters succeed` () {
        val opts = listOf(
            "channelCacheSize=50",
            "heartbeat=25",
            "automaticRecovery=true",
            "recoveryInterval=5",
            "shutdownTimeout=5"
        )
        val opt = opts.joinToString("&")
        val uri = "amqp://user:pass@localhost:12345?$opt"
        val cf = createConnectionFactory(URI(uri))
        assert(cf.host == "localhost")
        assert(cf.port == 12345)
    }

    fun `rabbit client disconnects properly` () {
        val client = RabbitMqClient(URI("amqp://guest:guest@localhost"))
        assert(client.connected)
        client.close()
        assert(!client.connected)
        client.close()
        assert(!client.connected)
    }

    fun `consumers handle numbers properly` () {
        val consumer = RabbitMqClient(URI("amqp://guest:guest@localhost"))
        consumer.declareQueue("int_op")
        consumer.declareQueue("long_op")
        consumer.declareQueue("list_op")
        consumer.consume("int_op", String::class, String::toInt)
        consumer.consume("long_op", String::class, String::toLong)
        consumer.consume("list_op", List::class) { it }

        val client = RabbitMqClient(URI("amqp://guest:guest@localhost"))
        assert(client.call("int_op", "123") == "123")
        assert(client.call("long_op", "456") == "456")
        assert(client.call("list_op", listOf(1, 3, 4).serialize()) == listOf(1, 3, 4).serialize())

        client.close()
        consumer.deleteQueue("int_op")
        consumer.deleteQueue("long_op")
        consumer.deleteQueue("list_op")
        consumer.close()
    }

    fun `consumers_handle_no_reply_messages` () {
        val consumer = RabbitMqClient(URI("amqp://guest:guest@localhost"))
        consumer.declareQueue("int_handler")
        consumer.declareQueue("long_handler")
        consumer.declareQueue("exception_handler")
        consumer.consume("int_handler", String::class) { info(it) }
        consumer.consume("long_handler", String::class) { info(it) }
        consumer.consume("exception_handler", String::class) { throw RuntimeException(it) }

        val client = RabbitMqClient(URI("amqp://guest:guest@localhost"))
        client.publish("int_handler", "123")
        client.publish("long_handler", "456")
        client.publish("exception_handler", "error")
        client.publish("exception_handler", "")

        client.close()
        consumer.deleteQueue("int_handler")
        consumer.deleteQueue("long_handler")
        consumer.deleteQueue("exception_handler")
        consumer.close()
    }
}
