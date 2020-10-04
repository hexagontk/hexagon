package com.hexagonkt.injection

import com.hexagonkt.helpers.Logger
import com.hexagonkt.helpers.eol
import kotlin.reflect.KClass

/**
 * Generators registry and utilities. This object keep tracks of supplier functions or specific
 * objects bound to classes. Different suppliers can be bound to the same type using 'tags'.
 */
object InjectionManager {

    internal val logger: Logger by lazy { Logger(this) }

    internal var registry: Map<Pair<KClass<*>, *>, () -> Any> = emptyMap()

    fun <T : Any, R : T> bind(type: KClass<T>, tag: Any, provider: () -> R) {
        val key = type to tag
        if (!registry.containsKey(key))
            registry = registry + (key to provider)
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
    fun <T : Any> injectOrNull(type: KClass<T>, tag: Any): T? =
        registry[type to tag]?.invoke() as? T

    @Suppress("UNCHECKED_CAST") // bind operation takes care of type matching
    fun <T : Any> inject(type: KClass<T>, tag: Any): T =
        injectOrNull(type, tag) ?: error("${type.java.name} generator missing")

    inline fun <reified T : Any> inject(tag: Any): T =
        inject(T::class, tag)

    fun <T : Any> inject(type: KClass<T>): T =
        inject(type, Unit)

    inline fun <reified T : Any> inject(): T =
        inject(T::class)

    inline fun <reified T : Any> injectOrNull(tag: Any): T? =
        injectOrNull(T::class, tag)

    fun <T : Any> injectOrNull(type: KClass<T>): T? =
        injectOrNull(type, Unit)

    inline fun <reified T : Any> injectOrNull(): T? =
        injectOrNull(T::class)

    override fun toString(): String =
        registry
            .map { it.key }
            .map { it.first.java.name to if (it.second is Unit) "" else " (${it.second})"}
            .joinToString(eol, "Bound classes with parameters:\n") {
                "\t * ${it.first}${it.second}"
            }
}
