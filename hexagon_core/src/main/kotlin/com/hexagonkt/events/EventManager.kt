package com.hexagonkt.events

import com.hexagonkt.helpers.CachedLogger
import kotlin.reflect.KClass

object EventManager : CachedLogger(EventManager::class) {
    var engine: EventEngine? = null

    fun <T : Event> consume(type: KClass<T>, address: String, consumer: (T) -> Unit) {
        engine?.consume(type, address, consumer) ?: error("Event engine not set")
    }

    fun <T : Event> consume(type: KClass<T>, consumer: (T) -> Unit) {
        consume(type, type.java.name, consumer)
    }

    fun publish(event: Event, address: String) {
        engine?.publish(event, address) ?: error("Event engine not set")
    }

    fun publish(event: Event) {
        publish(event, event.javaClass.name)
    }
}
