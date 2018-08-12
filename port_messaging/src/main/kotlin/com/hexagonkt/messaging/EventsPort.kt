package com.hexagonkt.messaging

import kotlin.reflect.KClass

// TODO 'unsubscribe' and 'call' (publish and wait response)
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
