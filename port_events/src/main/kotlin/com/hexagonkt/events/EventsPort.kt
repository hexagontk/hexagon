package com.hexagonkt.events

import kotlin.reflect.KClass

interface EventsPort {
    fun <T : Event> consume(type: KClass<T>, address: String, consumer: (T) -> Unit)

    fun publish(event: Event, address: String)

    fun <T : Event> consume(type: KClass<T>, consumer: (T) -> Unit) {
        consume(type, type.java.name, consumer)
    }

    fun publish(event: Event) {
        publish(event, event.javaClass.name)
    }
}
