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

    inline fun <reified T : Any> bindInstances(instances: Map<Any, T>) {
        bind(T::class, instances.mapValues { Instance(it.value) })
    }

    inline fun <reified T : Any> bindInstances(instances: List<T>) {
        bind(T::class, instances.map { Instance(it) })
    }

    inline fun <reified T : Any> bindInstances(vararg instances: Pair<Any, T>) {
        bindInstances(instances.associate { (k, v) -> k to v })
    }

    inline fun <reified T : Any> bindInstances(vararg instances: T) {
        bindInstances(instances.map { it })
    }

    inline fun <reified T : Any> bindGenerators(generators: Map<Any, () -> T>) {
        bind(T::class, generators.mapValues { Generator(it.value) })
    }

    inline fun <reified T : Any> bindGenerators(generators: List<() -> T>) {
        bind(T::class, generators.map { Generator(it) })
    }

    inline fun <reified T : Any> bindGenerators(vararg generators: Pair<Any, () -> T>) {
        bindGenerators(generators.toMap())
    }

    inline fun <reified T : Any> bindGenerators(vararg generators: () -> T) {
        bindGenerators(generators.map { it })
    }

    override fun toString(): String =
        bindings
            .map { it.key }
            .map { it.type.java.name to if (it.tag is Unit) "" else " (${it.tag})"}
            .joinToString(eol, "Bound classes with parameters:\n") {
                "\t * ${it.first}${it.second}"
            }
}
