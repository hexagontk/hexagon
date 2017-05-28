package co.there4.hexagon.events.rabbitmq

import co.there4.hexagon.events.Event
import co.there4.hexagon.events.EventEngine
import co.there4.hexagon.serialization.serialize
import java.net.URI
import kotlin.reflect.KClass

/**
 * TODO .
 */
class RabbitMqEventEngine : EventEngine {
    private companion object {
        private const val exchange = "events"
    }

    private val client by lazy { RabbitClient(URI("amqp://guest:guest@localhost")) }

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
