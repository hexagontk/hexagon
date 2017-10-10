package com.hexagonkt.events

import kotlin.reflect.KClass

interface EventEngine {
    fun <T : Event> consume(type: KClass<T>, address: String, consumer: (T) -> Unit)
    fun publish(event: Event, address: String)
}
