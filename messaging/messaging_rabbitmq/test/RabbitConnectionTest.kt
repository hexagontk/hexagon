package com.hexagontk.messaging.rabbitmq

import com.hexagontk.core.loggerOf
import com.hexagontk.core.error
import com.hexagontk.serialization.jackson.json.Json

import org.junit.jupiter.api.AfterAll
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.Disabled
import kotlin.test.Test
import org.junit.jupiter.api.TestInstance
import org.junit.jupiter.api.TestInstance.Lifecycle.PER_CLASS
import org.junit.jupiter.api.condition.DisabledInNativeImage
import org.junit.jupiter.api.condition.DisabledOnOs
import org.junit.jupiter.api.condition.OS
import org.testcontainers.containers.RabbitMQContainer
import org.testcontainers.utility.DockerImageName
import java.lang.System.Logger
import java.lang.System.currentTimeMillis
import java.net.URI

@TestInstance(PER_CLASS)
@DisabledOnOs(OS.WINDOWS)
@DisabledInNativeImage // TODO Fix for native image
internal class RabbitConnectionTest {

    private companion object {
        val rabbitMq: RabbitMQContainer = RabbitMQContainer(DockerImageName.parse("rabbitmq:3.11-alpine"))
            .withExposedPorts(5672)
            .apply { start() }

        val PORT: Int = rabbitMq.getMappedPort(5672)
        val URI: String = "amqp://guest:guest@localhost:$PORT"

        private const val QUEUE: String = "test"
        private const val QUEUE_ERROR: String = "error"
        private const val SUFFIX: String = "DONE"
        private const val DELAY: Long = 10L
    }

    private val log: Logger = loggerOf(this::class)

    private val consumer: RabbitMqClient by lazy { RabbitMqClient(URI(URI), Json) }
    private val client: RabbitMqClient by lazy { RabbitMqClient(URI(URI), Json) }

    @BeforeAll fun startConsumer() {
        rabbitMq.start()

        consumer.declareQueue(QUEUE)
        consumer.consume(QUEUE, String::class, { it.toString() }) { a ->
            Thread.sleep(DELAY)
            a + SUFFIX
        }

        consumer.declareQueue(QUEUE_ERROR)
        consumer.consume(QUEUE_ERROR, String::class, { it.toString() }) { a ->
            throw RuntimeException("Error with: $a")
        }
    }

    @AfterAll fun deleteTestQueue() {
        consumer.deleteQueue(QUEUE)
        consumer.deleteQueue(QUEUE_ERROR)
        consumer.close()

        rabbitMq.stop()
    }

    @Test
    @Disabled // TODO Review this test
    fun `call return expected results`() {
        val ts = currentTimeMillis().toString()
        assert(client.call(QUEUE, ts) == ts + SUFFIX)
        val result = client.call(QUEUE_ERROR, ts)
        assert(result.contains(ts) && result.contains("Error with: $ts"))

        rabbitMq.stop()
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
