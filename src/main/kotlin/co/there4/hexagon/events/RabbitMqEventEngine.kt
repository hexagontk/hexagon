package co.there4.hexagon.events

import co.there4.hexagon.events.rabbitmq.RabbitClient
import co.there4.hexagon.serialization.serialize
import kotlin.reflect.KClass

class RabbitMqEventEngine : EventEngine {
    val exchange = "events"

    val client by lazy { RabbitClient() }

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
