package co.there4.hexagon.events

import co.there4.hexagon.events.rabbitmq.RabbitMqEventEngine
import co.there4.hexagon.helpers.CachedLogger
import kotlin.reflect.KClass

object EventManager : CachedLogger(EventManager::class) {
    var engine: EventEngine = RabbitMqEventEngine()

    fun <T : Event> consume(type: KClass<T>, address: String, consumer: (T) -> Unit) {
        engine.consume(type, address, consumer)
    }

    fun <T : Event> consume(type: KClass<T>, consumer: (T) -> Unit) {
        consume(type, type.java.name, consumer)
    }

    fun publish(event: Event, address: String) {
        engine.publish(event, address)
    }

    fun publish(event: Event) {
        publish(event, event.javaClass.name)
    }
}
