package com.hexagonkt.injection

import kotlin.reflect.KClass

fun <T : Any, R : T> forceBind(type: KClass<T>, tag: Any, provider: () -> R) {
    val key = type to tag

    if (InjectionManager.registry.containsKey(key))
        InjectionManager.logger.info { "" }

    InjectionManager.registry = InjectionManager.registry + (key to provider)
}

fun <T : Any, R : T> forceBind(type: KClass<T>, provider: () -> R) {
    forceBind(type, Unit, provider)
}

fun <T : Any, R : T> forceBindObject(type: KClass<T>, tag: Any, instance: R) {
    forceBind(type, tag) { instance }
}

fun <T : Any, R : T> forceBindObject(type: KClass<T>, instance: R) {
    forceBindObject(type, Unit, instance)
}

inline fun <reified T : Any> forceBind(tag: Any, noinline provider: () -> T) =
    forceBind(T::class, tag, provider)

inline fun <reified T : Any> forceBindObject(tag: Any, instance: T) =
    forceBindObject(T::class, tag, instance)

inline fun <reified T : Any> forceBind(noinline provider: () -> T) =
    forceBind(T::class, provider)

inline fun <reified T : Any> forceBindObject(instance: T) =
    forceBindObject(T::class, instance)
