package com.hexagonkt.injection

import kotlin.reflect.KClass
import com.hexagonkt.injection.InjectionManager.logger

data class Target<T : Any>(
    val type: KClass<T>,
    val tag: Any = Unit,
)

sealed class Provider<out T : Any> {
    abstract fun provide(): T
}

data class Generator<out T : Any>(val generator: () -> T) : Provider<T>() {
    override fun provide(): T =
        generator()
}

data class Instance<out T : Any>(val instance: T) : Provider<T>() {
    override fun provide(): T =
        instance
}

fun <T : Any, R : T> forceBind(type: KClass<T>, provider: Provider<R>, tag: Any = Unit) {
    val key = Target(type, tag)

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

fun <T : Any, R : T> forceBind(type: KClass<T>, tag: Any = Unit, provider: () -> R) {
    forceBind(type, Generator(provider), tag)
}

fun <T : Any, R : T> forceBind(type: KClass<T>, tag: Any, instance: R) {
    forceBind(type, Instance(instance), tag)
}

fun <T : Any, R : T> forceBind(type: KClass<T>, instance: R) {
    forceBind(type, Unit, instance)
}

fun <T : Any, R : T> forceBindSet(type: KClass<T>, instances: List<Provider<R>>) {
    instances.forEachIndexed { ii, instance -> forceBind(type, instance, ii) }
}

fun <T : Any, R : T> forceBindSet(type: KClass<T>, providers: Map<Any, Provider<R>>) {
    providers.forEach { (k, v) -> forceBind(type, v, k) }
}
