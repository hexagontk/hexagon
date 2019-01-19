package com.hexagonkt.injection

import kotlin.reflect.KClass

/**
 * Generators registry and utilities.
 */
object InjectionManager {
    private var registry: Map<Pair<KClass<*>, *>, () -> Any> = emptyMap()

    fun <T : Any, R : T> bind(type: KClass<T>, parameter: Any, provider: () -> R) {
        registry += (type to parameter) to provider
    }

    fun <T : Any, R : T> bind(type: KClass<T>, provider: () -> R) {
        bind(type, Unit, provider)
    }

    fun <T : Any, R : T> bindObject(type: KClass<T>, parameter: Any, instance: R) {
        bind(type, parameter) { instance }
    }

    fun <T : Any, R : T> bindObject(type: KClass<T>, instance: R) {
        bindObject(type, Unit, instance)
    }

    inline fun <reified T : Any> bind(parameter: Any, noinline provider: () -> T) =
        bind(T::class, parameter, provider)

    inline fun <reified T : Any> bindObject(parameter: Any, instance: T) =
        bindObject(T::class, parameter, instance)

    inline fun <reified T : Any> bind(noinline provider: () -> T) =
        bind(T::class, provider)

    inline fun <reified T : Any> bindObject(instance: T) =
        bindObject(T::class, instance)

    @Suppress("UNCHECKED_CAST") // bind operation takes care of type matching
    fun <T : Any> inject(type: KClass<T>, parameter: Any): T =
        registry[type to parameter]?.invoke() as? T ?: error("${type.java.name} generator missing")

    inline fun <reified T : Any> inject(parameter: Any): T = inject(T::class, parameter)

    fun <T : Any> inject(type: KClass<T>): T = inject(type, Unit)

    inline fun <reified T : Any> inject(): T = inject(T::class)
}
