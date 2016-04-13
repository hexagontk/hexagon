package co.there4.hexagon.events

import co.there4.hexagon.messaging.RabbitClient
import co.there4.hexagon.serialization.serialize
import co.there4.hexagon.util.CompanionLogger
import kotlin.reflect.KClass

class EventManager {
    companion object : CompanionLogger (EventManager::class) {
        val EXCHANGE = "events"
    }

    val client = RabbitClient ("amqp://guest:guest@localhost")

    fun <T : Event> on(type: KClass<T>, event: String, consumer: (T) -> Unit) {
        client.consume(EXCHANGE, event, type) { consumer(it) }
    }

    fun publish(event: Event) {
        client.publish(EXCHANGE, event.action, event.serialize())
    }
}
