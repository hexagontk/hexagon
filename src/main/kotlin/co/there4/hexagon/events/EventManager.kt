package co.there4.hexagon.events

import co.there4.hexagon.messaging.RabbitClient
import co.there4.hexagon.serialization.serialize
import co.there4.hexagon.util.CompanionLogger

import kotlin.reflect.KClass

object EventManager : CompanionLogger (EventManager::class) {
    const val exchange = "events"

    val client by lazy { RabbitClient () }

    init {
        client.bindExchange(exchange, "topic", "*.*.*", "event_pool")
    }

    fun <T : Event> consume(type: KClass<T>, event: String, consumer: (T) -> Unit) {
        client.consume(exchange, event, type) { consumer(it) }
    }

    fun <T : Event> consume(type: KClass<T>,  consumer: (T) -> Unit) {
        consume(type, type.java.name, consumer)
    }

    fun publish(event: Event) {
        client.publish(exchange, event.action, event.serialize())
    }
}
