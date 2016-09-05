package co.there4.hexagon.events

import co.there4.hexagon.messaging.RabbitClient
import co.there4.hexagon.serialization.serialize
import co.there4.hexagon.util.CompanionLogger

object EventManager : CompanionLogger (EventManager::class) {
    val exchange = "events"
    val client = RabbitClient ()

    init {
        client.bindExchange(exchange, "topic", "*.*.*", "event_pool")
    }

    inline fun <reified T : Event> on(event: String, crossinline consumer: (T) -> Unit) {
        client.consume(exchange, event, T::class) { consumer(it) }
    }

    inline fun <reified T : Event> on(crossinline consumer: (T) -> Unit) {
        on(T::class.java.name, consumer)
    }

    fun publish(event: Event) {
        client.publish(exchange, event.action, event.serialize())
    }
}
