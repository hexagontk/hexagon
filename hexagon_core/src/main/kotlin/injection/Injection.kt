package com.hexagonkt.injection

import kotlin.reflect.KClass
import com.hexagonkt.injection.InjectionManager.logger

fun <T : Any, R : T> forceBind(type: KClass<T>, tag: Any, provider: () -> R) {
    val key = type to tag

    val binding = InjectionManager.binding(key)
    if (InjectionManager.bindings.containsKey(key)) {
        InjectionManager.bindings = InjectionManager.bindings - key // Required to change order
        logger.warn { "$binding already bound (OVERRIDDEN). This should happen ONLY IN TEST" }
    }
    else {
        logger.info { "$binding bound to function (FORCED). This should happen ONLY IN TEST" }
    }

    InjectionManager.bindings = InjectionManager.bindings + (key to provider)
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

fun <T : Any, R : T> forceBind(type: KClass<T>, providers: List<() -> R>) {
    providers.forEachIndexed { ii, provider -> forceBind(type, ii, provider) }
}

inline fun <reified T : Any> forceBind(providers: List<() -> T>) {
    forceBind(T::class, providers)
}

fun <T : Any, R : T> forceBindObjects(type: KClass<T>, instances: List<R>) {
    instances.forEachIndexed { ii, instance -> forceBindObject(type, ii, instance) }
}

inline fun <reified T : Any> forceBindObjects(instances: List<T>) {
    forceBindObjects(T::class, instances)
}

fun <T : Any, R : T> forceBind(type: KClass<T>, providers: Map<Any, () -> R>) {
    providers.forEach { (k, v) -> forceBind(type, k, v) }
}

inline fun <reified T : Any> forceBind(providers: Map<Any, () -> T>) {
    forceBind(T::class, providers)
}

fun <T : Any, R : T> forceBindObjects(type: KClass<T>, instances: Map<Any, R>) {
    instances.forEach { (k, v) -> forceBindObject(type, k, v) }
}

inline fun <reified T : Any> forceBindObjects(instances: Map<Any, T>) {
    forceBindObjects(T::class, instances)
}
