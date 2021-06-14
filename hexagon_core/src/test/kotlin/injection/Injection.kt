package com.hexagonkt.injection

import com.hexagonkt.injection.Provider.Generator
import com.hexagonkt.injection.Provider.Instance
import kotlin.reflect.KClass

fun <T : Any, R : T> Module.forceBind(type: KClass<T>, provider: Provider<R>, tag: Any = Unit) {
    val key = Target(type, tag)
    val binding = key.toString()

    check(bindings.containsKey(key)) {
        "$binding not bound. Override only allowed in tests (using `forceBind`)"
    }

    bindings = bindings - key // Required to change order
    logger.info { "$binding bound to new generator (OVERRIDDEN)" }

    bindings = bindings + (key to provider)
}

fun <T : Any, R : T> Module.forceBind(type: KClass<T>, tag: Any = Unit, provider: () -> R) {
    forceBind(type, Generator(provider), tag)
}

fun <T : Any, R : T> Module.forceBind(type: KClass<T>, tag: Any, instance: R) {
    forceBind(type, Instance(instance), tag)
}

fun <T : Any, R : T> Module.forceBind(type: KClass<T>, instance: R) {
    forceBind(type, Unit, instance)
}

fun <T : Any, R : T> Module.forceBindSet(type: KClass<T>, instances: List<Provider<R>>) {
    instances.forEachIndexed { ii, instance -> forceBind(type, instance, ii) }
}

fun <T : Any, R : T> Module.forceBindSet(type: KClass<T>, providers: Map<Any, Provider<R>>) {
    providers.forEach { (k, v) -> forceBind(type, v, k) }
}
