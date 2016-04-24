package co.there4.hexagon.events

import co.there4.hexagon.messaging.RabbitClient
import co.there4.hexagon.serialization.serialize
import co.there4.hexagon.util.CompanionLogger
import kotlin.reflect.KClass

object EventManager : CompanionLogger (EventManager::class) {
    private val exchange = "events"

    val client = RabbitClient ("amqp://guest:guest@localhost")

    fun <T : Event> on(type: KClass<T>, event: String, consumer: (T) -> Unit) {
        client.consume(exchange, event, type) { consumer(it) }
    }

    fun publish(event: Event) {
        client.publish(exchange, event.action, event.serialize())
    }
}
