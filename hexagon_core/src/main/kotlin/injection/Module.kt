package com.hexagonkt.injection

import com.hexagonkt.helpers.eol
import com.hexagonkt.injection.Provider.Generator
import com.hexagonkt.injection.Provider.Instance
import com.hexagonkt.logging.Logger
import kotlin.reflect.KClass

class Module {

    internal val logger: Logger by lazy { Logger(this::class) }
    internal var bindings: Map<Target<out Any>, Provider<Any>> = emptyMap()

    fun clear() {
        bindings = emptyMap()
    }

    fun <T : Any> bind(type: KClass<T>, providers: Map<Any, Provider<T>>) {
        providers.forEach { (k, v) -> bind(Target(type, k), v) }
    }

    fun <T : Any> bind(type: KClass<T>, providers: List<Provider<T>>) {
        providers.forEachIndexed { ii, it -> bind(Target(type, ii), it) }
    }

    fun <T : Any> bind(target: Target<T>, provider: Provider<T>) {
        val binding = target.toString()

        check(!bindings.containsKey(target)) {
            "$binding already bound. Override only allowed in tests (using `forceBind`)"
        }

        bindings = bindings + (target to provider)
        logger.info { "$binding bound to function" }
    }

    inline fun <reified T : Any> bind(instance: T) {
        bind(Target(T::class), Instance(instance))
    }

    inline fun <reified T : Any> bind(noinline generator: () -> T) {
        bind(Target(T::class), Generator(generator))
    }

    inline fun <reified T : Any> bind(tag: Any, instance: T) {
        bind(Target(T::class, tag), Instance(instance))
    }

    inline fun <reified T : Any> bind(tag: Any, noinline generator: () -> T) {
        bind(Target(T::class, tag), Generator(generator))
    }

    inline fun <reified T : Any> bindInstances(providers: Map<Any, T>) {
        bind(T::class, providers.mapValues { Instance(it.value) })
    }

    inline fun <reified T : Any> bindInstances(providers: List<T>) {
        bind(T::class, providers.map { Instance(it) })
    }

    inline fun <reified T : Any> bindInstances(vararg providers: Pair<Any, T>) {
        bindInstances(providers.associate { (k, v) -> k to v })
    }

    inline fun <reified T : Any> bindInstances(vararg providers: T) {
        bindInstances(providers.map { it })
    }

    inline fun <reified T : Any> bindGenerators(providers: Map<Any, () -> T>) {
        bind(T::class, providers.mapValues { Generator(it.value) })
    }

    inline fun <reified T : Any> bindGenerators(providers: List<() -> T>) {
        bind(T::class, providers.map { Generator(it) })
    }

    inline fun <reified T : Any> bindGenerators(vararg providers: Pair<Any, () -> T>) {
        bindGenerators(providers.toMap())
    }

    inline fun <reified T : Any> bindGenerators(vararg providers: () -> T) {
        bindGenerators(providers.map { it })
    }

    override fun toString(): String =
        bindings
            .map { it.key }
            .map { it.type.java.name to if (it.tag is Unit) "" else " (${it.tag})"}
            .joinToString(eol, "Bound classes with parameters:\n") {
                "\t * ${it.first}${it.second}"
            }
}
