package co.there4.hexagon.events

import co.there4.hexagon.events.rabbitmq.RabbitClient
import co.there4.hexagon.serialization.serialize
import co.there4.hexagon.util.CachedLogger

import kotlin.reflect.KClass

interface EventBackend {
    fun <T : Event> consume(type: KClass<T>, address: String, consumer: (T) -> Unit)
    fun publish(event: Event, address: String)
}

class RabbitMqEventBackend : EventBackend {
    val exchange = "events"

    val client by lazy { RabbitClient () }

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

object EventManager : CachedLogger(EventManager::class) {
    var backend: EventBackend = RabbitMqEventBackend()

    fun <T : Event> consume(type: KClass<T>, address: String, consumer: (T) -> Unit) {
        backend.consume(type, address, consumer)
    }

    fun <T : Event> consume(type: KClass<T>, consumer: (T) -> Unit) {
        consume(type, type.java.name, consumer)
    }

    fun publish(event: Event, address: String) {
        backend.publish(event, address)
    }

    fun publish(event: Event) {
        publish(event, event.javaClass.name)
    }
}
