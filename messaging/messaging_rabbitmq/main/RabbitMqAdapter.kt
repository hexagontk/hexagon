package com.hexagontk.messaging.rabbitmq

import com.hexagontk.messaging.Message
import com.hexagontk.messaging.MessagingPort
import com.hexagontk.serialization.SerializationFormat
import com.hexagontk.serialization.serialize
import java.net.URI
import kotlin.reflect.KClass

class RabbitMqAdapter(
    url: String = "amqp://guest:guest@localhost",
    private val serializationFormat: SerializationFormat
) : MessagingPort {

    private companion object {
        private const val EXCHANGE = "messages"
    }

    private val client by lazy { RabbitMqClient(URI(url), serializationFormat) }

    init {
        client.bindExchange(EXCHANGE, "topic", "*.*.*", "event_pool")
    }

    override fun <T : Message> consume(
        type: KClass<T>, address: String, decoder: (Map<String, *>) -> T, consumer: (T) -> Unit
    ) {
        client.consume(EXCHANGE, address, type, decoder) { consumer(it) }
    }

    override fun publish(message: Message, address: String) {
        client.publish(EXCHANGE, address, message.serialize(serializationFormat))
    }
}
