package com.hexagonkt.injection

import kotlin.reflect.KClass

/**
 * Generators registry and utilities.
 */
object InjectionManager {
    private var registry: Map<Pair<KClass<*>, *>, () -> Any> = emptyMap()

    fun <T : Any, R : T> bind(type: KClass<T>, tag: Any, provider: () -> R) {
        registry = registry + ((type to tag) to provider)
    }

    fun <T : Any, R : T> bind(type: KClass<T>, provider: () -> R) {
        bind(type, Unit, provider)
    }

    fun <T : Any, R : T> bindObject(type: KClass<T>, tag: Any, instance: R) {
        bind(type, tag) { instance }
    }

    fun <T : Any, R : T> bindObject(type: KClass<T>, instance: R) {
        bindObject(type, Unit, instance)
    }

    inline fun <reified T : Any> bind(tag: Any, noinline provider: () -> T) =
        bind(T::class, tag, provider)

    inline fun <reified T : Any> bindObject(tag: Any, instance: T) =
        bindObject(T::class, tag, instance)

    inline fun <reified T : Any> bind(noinline provider: () -> T) =
        bind(T::class, provider)

    inline fun <reified T : Any> bindObject(instance: T) =
        bindObject(T::class, instance)

    @Suppress("UNCHECKED_CAST") // bind operation takes care of type matching
    fun <T : Any> inject(type: KClass<T>, tag: Any): T =
        registry[type to tag]?.invoke() as? T ?: error("${type.java.name} generator missing")

    inline fun <reified T : Any> inject(tag: Any): T = inject(T::class, tag)

    fun <T : Any> inject(type: KClass<T>): T = inject(type, Unit)

    inline fun <reified T : Any> inject(): T = inject(T::class)

    operator fun invoke(block: InjectionManager.() -> Unit): InjectionManager {
        this.apply(block)
        return this
    }
}
