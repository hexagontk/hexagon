package com.hexagonkt.messaging

import kotlin.reflect.KClass

// TODO 'unsubscribe' and 'call' (publish and wait response)
interface MessagingPort {
    fun <T : Message> consume(type: KClass<T>, address: String, consumer: (T) -> Unit)

    fun publish(message: Message, address: String)

    fun <T : Message> consume(type: KClass<T>, consumer: (T) -> Unit) {
        consume(type, type.java.name, consumer)
    }

    fun publish(message: Message) {
        publish(message, message.javaClass.name)
    }
}
