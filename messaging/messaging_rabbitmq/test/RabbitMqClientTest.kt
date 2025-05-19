package com.hexagontk.messaging.rabbitmq

import com.hexagontk.core.info
import com.hexagontk.core.loggerOf
import com.hexagontk.messaging.rabbitmq.RabbitMqClient.Companion.createConnectionFactory
import com.hexagontk.messaging.rabbitmq.RabbitTest.Companion.PORT
import com.hexagontk.serialization.jackson.json.Json
import com.hexagontk.serialization.serialize
import junit.framework.TestCase.assertEquals
import org.junit.jupiter.api.Disabled

import kotlin.test.Test
import org.junit.jupiter.api.TestInstance
import org.junit.jupiter.api.TestInstance.Lifecycle.PER_CLASS
import org.junit.jupiter.api.condition.DisabledOnOs
import org.junit.jupiter.api.condition.OS
import java.lang.System.Logger
import java.net.URI
import kotlin.test.assertFailsWith

@TestInstance(PER_CLASS)
@DisabledOnOs(OS.WINDOWS)
internal class RabbitMqClientTest {

    private val log: Logger = loggerOf(this::class)

    @Test fun `Create a connection factory with empty URI fails` () {
        assertFailsWith(IllegalArgumentException::class) {
            createConnectionFactory(URI(""))
        }
    }

    @Test fun `Create a connection factory with invalid URI fails` () {
        assertFailsWith(IllegalArgumentException::class) {
            createConnectionFactory(URI("http://localhost"))
        }
    }

    @Test fun `Create a connection factory without parameters succeed` () {
        val uri = "amqp://user:pass@localhost:12345"
        val cf = createConnectionFactory(URI(uri))
        assert(cf.host == "localhost")
        assert(cf.port == 12345)
    }

    @Test fun `Create a connection factory with one parameter succeed` () {
        val uri = "amqp://user:pass@localhost:12345?channelCacheSize=50"
        val cf = createConnectionFactory(URI(uri))
        assert(cf.host == "localhost")
        assert(cf.port == 12345)
    }

    @Test fun `Create a connection factory with two parameter succeed` () {
        val uri = "amqp://user:pass@localhost:12345?channelCacheSize=50&heartbeat=25"
        val cf = createConnectionFactory(URI(uri))
        assert(cf.host == "localhost")
        assert(cf.port == 12345)
    }

    @Test fun `Create a connection factory with all parameters succeed` () {
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

    @Test fun `Create a connection factory with empty parameters succeed` () {
        val opts = listOf(
            "channelCacheSize",
            "heartbeat=10",
            "automaticRecovery",
            "recoveryInterval",
            "shutdownTimeout"
        )
        val opt = opts.joinToString("&")
        val uri = "amqp://user:pass@localhost:12345?$opt"
        val cf = createConnectionFactory(URI(uri))
        assert(cf.host == "localhost")
        assert(cf.port == 12345)
    }

    @Test fun `Rabbit client disconnects properly` () {
        val client = RabbitMqClient(URI("amqp://guest:guest@localhost:$PORT"), Json)
        assert(client.connected)
        client.close()
        assert(!client.connected)
        client.close()
        assert(!client.connected)
    }

    @Test
    @Disabled // TODO Fix code
    fun `Consumers handle numbers properly` () {
        val consumer = RabbitMqClient(URI("amqp://guest:guest@localhost:$PORT"), Json)
        consumer.declareQueue("int_op")
        consumer.declareQueue("long_op")
        consumer.declareQueue("list_op")
        consumer.consume("int_op", String::class, { it.toString() }, String::toInt)
        consumer.consume("long_op", String::class, { it.toString() }, String::toLong)
        consumer.consume("list_op", List::class, { it.map { x -> x.toString() }}) { it }

        val client = RabbitMqClient(URI("amqp://guest:guest@localhost:$PORT"), Json)
        assert(client.call("int_op", "123") == "123")
        assert(client.call("long_op", "456") == "456")
        assertEquals(
            client.call("list_op", listOf(1, 3, 4).serialize(Json)),
            listOf(1, 3, 4).serialize(Json)
        )

        client.close()
        consumer.deleteQueue("int_op")
        consumer.deleteQueue("long_op")
        consumer.deleteQueue("list_op")
        consumer.close()
    }

    @Test fun `Consumers handle no reply messages` () {
        val consumer = RabbitMqClient(URI("amqp://guest:guest@localhost:$PORT"), Json)
        consumer.declareQueue("int_handler")
        consumer.declareQueue("long_handler")
        consumer.declareQueue("exception_handler")
        consumer.consume("int_handler", String::class, { it.toString() }) { log.info { it } }
        consumer.consume("long_handler", String::class, { it.toString() }) { log.info { it } }
        consumer.consume("exception_handler", String::class, { it.toString() }) {
            throw RuntimeException(it)
        }

        val client = RabbitMqClient(URI("amqp://guest:guest@localhost:$PORT"), Json)
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
