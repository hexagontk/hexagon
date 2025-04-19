package com.hexagontk.injection

import kotlin.reflect.KClass

/**
 * Inject type instances based on the suppliers defined in the [Module].
 */
class Injector(private val module: Module = Module()) {

    /**
     * Inject an instance of the type class and the supplied tag. If the injector's module doesn't
     * have a matching binding, then `null` is returned.
     *
     * @param T Generic type of the instance that will be created.
     * @param type Class for the instance to create (class of T).
     * @param tag Tag used to search the binding in the [Module].
     * @return An instance of T or `null` if no binding for that type with the passed tag is found.
     */
    @Suppress("UNCHECKED_CAST") // bind operation takes care of type matching
    fun <T : Any> injectOrNull(type: KClass<T>, tag: Any): T? =
        module.bindings[Target(type, tag)]?.provide() as? T

    @Suppress("UNCHECKED_CAST") // bind operation takes care of type matching
    fun <T : Any> injectList(type: KClass<T>): List<T> =
        module.bindings
            .filter { it.key.type == type }
            .map { it.value.provide() as T }

    @Suppress("UNCHECKED_CAST") // bind operation takes care of type matching
    fun <T : Any> injectMap(type: KClass<T>): Map<Any, T> =
        module.bindings
            .filter { it.key.type == type }
            .map { it.key.tag to it.value.provide() as T }
            .associate { it.first to it.second }

    fun <T : Any> inject(type: KClass<T>, tag: Any): T =
        injectOrNull(type, tag) ?: error("${type.java.name} generator missing")

    fun <T : Any> inject(type: KClass<T>): T =
        inject(type, Unit)

    fun <T : Any> injectOrNull(type: KClass<T>): T? =
        injectOrNull(type, Unit)

    inline fun <reified T : Any> inject(tag: Any): T =
        inject(T::class, tag)

    inline fun <reified T : Any> inject(): T =
        inject(T::class)

    inline fun <reified T : Any> injectOrNull(tag: Any): T? =
        injectOrNull(T::class, tag)

    inline fun <reified T : Any> injectOrNull(): T? =
        injectOrNull(T::class)

    inline fun <reified T : Any> injectList(): List<T> =
        injectList(T::class)

    inline fun <reified T : Any> injectMap(): Map<Any, T> =
        injectMap(T::class)
}
