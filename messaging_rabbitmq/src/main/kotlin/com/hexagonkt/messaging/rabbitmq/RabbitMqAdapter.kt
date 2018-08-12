package com.hexagonkt.messaging.rabbitmq

import com.hexagonkt.messaging.Message
import com.hexagonkt.messaging.MessagingPort
import com.hexagonkt.serialization.serialize
import java.net.URI
import kotlin.reflect.KClass

/**
 * TODO .
 */
class RabbitMqAdapter : MessagingPort {
    private companion object {
        private const val exchange = "events"
    }

    private val client by lazy { RabbitMqClient(URI("amqp://guest:guest@localhost")) }

    init {
        client.bindExchange(exchange, "topic", "*.*.*", "event_pool")
    }

    override fun <T : Message> consume(type: KClass<T>, address: String, consumer: (T) -> Unit) {
        client.consume(exchange, address, type) { consumer(it) }
    }

    override fun publish(message: Message, address: String) {
        client.publish(exchange, address, message.serialize())
    }
}
