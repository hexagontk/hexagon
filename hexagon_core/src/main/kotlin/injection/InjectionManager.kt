package com.hexagonkt.injection

import com.hexagonkt.logging.Logger
import com.hexagonkt.helpers.eol
import kotlin.reflect.KClass

/**
 * Generators registry and utilities. This object keep tracks of supplier functions or specific
 * objects bound to classes. Different suppliers can be bound to the same type using 'tags'.
 */
object InjectionManager {

    internal val logger: Logger by lazy { Logger(this::class) }

    internal var bindings: Map<Target<*>, Provider<*>> = emptyMap()

    internal fun binding(key: Target<*>): String =
        if (key.tag == Unit) key.type.toString()
        else "${key.type} with tag '${key.tag}'"

    fun <T : Any, R : T> bind(type: KClass<T>, provider: Provider<R>, tag: Any = Unit) {
        val key = Target(type, tag)

        val binding = binding(key)
        if (!bindings.containsKey(key)) {
            bindings = bindings + (key to provider)
            logger.info { "$binding bound to function" }
        }
        else {
            logger.warn { "$binding already bound (IGNORED). This should happen ONLY IN TEST" }
        }
    }

    fun <T : Any, R : T> bind(type: KClass<T>, tag: Any = Unit, provider: () -> R) {
        bind(type, Generator(provider), tag)
    }

    fun <T : Any, R : T> bind(type: KClass<T>, provider: () -> R) {
        bind(type, Generator(provider))
    }

    fun <T : Any, R : T> bind(type: KClass<T>, instance: R, tag: Any = Unit) {
        bind(type, Instance(instance), tag)
    }

//    fun <T : Any, R : T> bind(type: KClass<T>, instance: R) {
//        bind(type, instance, Unit)
//    }

    inline fun <reified T : Any> bind(tag: Any, noinline provider: () -> T) =
        bind(T::class, Generator(provider), tag)

    inline fun <reified T : Any> bind(noinline provider: () -> T) =
        bind(T::class, Generator(provider))

    inline fun <reified T : Any> bind(instance: T, tag: Any = Unit) =
        bind(T::class, instance, tag)

    fun <T : Any, R : T> bind(type: KClass<T>, providers: List<() -> R>) {
        providers.forEachIndexed { ii, provider -> bind(type, Generator(provider), ii) }
    }

    inline fun <reified T : Any> bind(providers: List<() -> T>) {
        bind(T::class, providers)
    }

    fun <T : Any, R : T> bindSet(type: KClass<T>, instances: List<R>) {
        instances.forEachIndexed { ii, instance -> bind(type, instance, ii) }
    }

    inline fun <reified T : Any> bindSet(instances: List<T>) {
        bindSet(T::class, instances)
    }

    fun <T : Any, R : T> bind(type: KClass<T>, providers: Map<Any, () -> R>) {
        providers.forEach { (k, v) -> bind(type, Generator(v), k) }
    }

    inline fun <reified T : Any> bind(providers: Map<Any, () -> T>) {
        bind(T::class, providers)
    }

    fun <T : Any, R : T> bindSet(type: KClass<T>, instances: Map<Any, R>) {
        instances.forEach { (k, v) -> bind(type, v, k) }
    }

    inline fun <reified T : Any> bindSet(instances: Map<Any, T>) {
        bindSet(T::class, instances)
    }

    @Suppress("UNCHECKED_CAST") // bind operation takes care of type matching
    fun <T : Any> injectOrNull(type: KClass<T>, tag: Any): T? =
        bindings[Target(type, tag)]?.provide() as? T

    @Suppress("UNCHECKED_CAST") // bind operation takes care of type matching
    fun <T : Any> injectList(type: KClass<T>): List<T> =
        bindings
            .filter { it.key.type == type }
            .map { it.value.provide() as T }

    @Suppress("UNCHECKED_CAST") // bind operation takes care of type matching
    fun <T : Any> injectMap(type: KClass<T>): Map<Any, T> =
        bindings
            .filter { it.key.type == type }
            .map { it.key.tag to it.value.provide() as T }
            .associate { it.first to it.second }

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
        bindings
            .map { it.key }
            .map { it.type.java.name to if (it.tag is Unit) "" else " (${it.tag})"}
            .joinToString(eol, "Bound classes with parameters:\n") {
                "\t * ${it.first}${it.second}"
            }
}
