package com.hexagonkt.injection

import kotlin.reflect.KClass

class Injector(private val module: Module = Module()) {

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

    @Suppress("UNCHECKED_CAST") // bind operation takes care of type matching
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
