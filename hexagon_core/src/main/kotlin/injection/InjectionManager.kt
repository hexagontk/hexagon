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

    internal var bindings: Map<Pair<KClass<*>, Any>, () -> Any> = emptyMap()

    internal fun binding(key: Pair<KClass<*>, Any>): String =
        if (key.second == Unit) key.first.toString()
        else "${key.first} with tag '${key.second}'"

    fun <T : Any, R : T> bind(type: KClass<T>, tag: Any, provider: () -> R) {
        val key = type to tag

        val binding = binding(key)
        if (!bindings.containsKey(key)) {
            bindings = bindings + (key to provider)
            logger.info { "$binding bound to function" }
        }
        else {
            logger.warn { "$binding already bound (IGNORED). This should happen ONLY IN TEST" }
        }
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

    fun <T : Any, R : T> bind(type: KClass<T>, providers: List<() -> R>) {
        providers.forEachIndexed { ii, provider -> bind(type, ii, provider) }
    }

    inline fun <reified T : Any> bind(providers: List<() -> T>) {
        bind(T::class, providers)
    }

    fun <T : Any, R : T> bindObjects(type: KClass<T>, instances: List<R>) {
        instances.forEachIndexed { ii, instance -> bindObject(type, ii, instance) }
    }

    inline fun <reified T : Any> bindObjects(instances: List<T>) {
        bindObjects(T::class, instances)
    }

    fun <T : Any, R : T> bind(type: KClass<T>, providers: Map<Any, () -> R>) {
        providers.forEach { (k, v) -> bind(type, k, v) }
    }

    inline fun <reified T : Any> bind(providers: Map<Any, () -> T>) {
        bind(T::class, providers)
    }

    fun <T : Any, R : T> bindObjects(type: KClass<T>, instances: Map<Any, R>) {
        instances.forEach { (k, v) ->  bindObject(type, k, v) }
    }

    inline fun <reified T : Any> bindObjects(instances: Map<Any, T>) {
        bindObjects(T::class, instances)
    }

    @Suppress("UNCHECKED_CAST") // bind operation takes care of type matching
    fun <T : Any> injectOrNull(type: KClass<T>, tag: Any): T? =
        bindings[type to tag]?.invoke() as? T

    @Suppress("UNCHECKED_CAST") // bind operation takes care of type matching
    fun <T : Any> injectList(type: KClass<T>): List<T> =
        bindings
            .filter { it.key.first == type }
            .map { it.value.invoke() as T }

    @Suppress("UNCHECKED_CAST") // bind operation takes care of type matching
    fun <T : Any> injectMap(type: KClass<T>): Map<Any, T> =
        bindings
            .filter { it.key.first == type }
            .map { it.key.second to it.value.invoke() as T }
            .map { it.first to it.second }
            .toMap()

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
            .map { it.first.java.name to if (it.second is Unit) "" else " (${it.second})"}
            .joinToString(eol, "Bound classes with parameters:\n") {
                "\t * ${it.first}${it.second}"
            }
}
