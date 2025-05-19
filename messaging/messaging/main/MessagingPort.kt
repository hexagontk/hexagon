package com.hexagontk.messaging

import kotlin.reflect.KClass

// TODO 'unsubscribe' and 'call' (publish and wait response)
interface MessagingPort {
    fun <T : Message> consume(
        type: KClass<T>, address: String, decoder: (Map<String, *>) -> T, consumer: (T) -> Unit
    )

    fun publish(message: Message, address: String)

    fun <T : Message> consume(
        type: KClass<T>, decoder: (Map<String, *>) -> T, consumer: (T) -> Unit
    ) {
        consume(type, type.java.name, decoder, consumer)
    }

    fun publish(message: Message) {
        publish(message, message.javaClass.name)
    }
}
