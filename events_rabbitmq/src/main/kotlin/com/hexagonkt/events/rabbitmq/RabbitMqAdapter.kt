package com.hexagonkt.events.rabbitmq

import com.hexagonkt.events.Event
import com.hexagonkt.events.EventsPort
import com.hexagonkt.serialization.serialize
import java.net.URI
import kotlin.reflect.KClass

/**
 * TODO .
 */
class RabbitMqAdapter : EventsPort {
    private companion object {
        private const val exchange = "events"
    }

    private val client by lazy { RabbitMqClient(URI("amqp://guest:guest@localhost")) }

    init {
        client.bindExchange(exchange, "topic", "*.*.*", "event_pool")
    }

    override fun <T : Event> consume(type: KClass<T>, address: String, consumer: (T) -> Unit) {
        client.consume(exchange, address, type) { consumer(it) }
    }

    override fun publish(event: Event, address: String) {
        client.publish(exchange, address, event.serialize())
    }
}
