package com.hexagonkt.events.rabbitmq

import com.hexagonkt.events.Event
import com.hexagonkt.events.EventEngine
import com.hexagonkt.serialization.serialize
import java.net.URI
import kotlin.reflect.KClass

/**
 * TODO .
 */
class RabbitMqEventEngine : EventEngine {
    private companion object {
        private const val exchange = "events"
    }

    private val client by lazy { com.hexagonkt.events.rabbitmq.RabbitMqClient(URI("amqp://guest:guest@localhost")) }

    init {
        client.bindExchange(com.hexagonkt.events.rabbitmq.RabbitMqEventEngine.Companion.exchange, "topic", "*.*.*", "event_pool")
    }

    override fun <T : Event> consume(type: KClass<T>, address: String, consumer: (T) -> Unit) {
        client.consume(com.hexagonkt.events.rabbitmq.RabbitMqEventEngine.Companion.exchange, address, type) { consumer(it) }
    }

    override fun publish(event: Event, address: String) {
        client.publish(com.hexagonkt.events.rabbitmq.RabbitMqEventEngine.Companion.exchange, address, event.serialize())
    }
}
