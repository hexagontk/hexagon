package com.hexagonkt.injection

import com.hexagonkt.injection.Provider.Generator
import com.hexagonkt.injection.Provider.Instance
import kotlin.reflect.KClass

fun <T : Any> Module.forceBind(type: KClass<T>, providers: Map<Any, Provider<T>>) {
    providers.forEach { (k, v) -> forceBind(Target(type, k), v) }
}

fun <T : Any> Module.forceBind(type: KClass<T>, providers: List<Provider<T>>) {
    providers.forEachIndexed { ii, it -> forceBind(Target(type, ii), it) }
}

fun <T : Any> Module.forceBind(target: Target<T>, provider: Provider<T>) {

    if (bindings.containsKey(target)) {
        logger.info { "$target already bound. Will be DELETED to allow OVERWRITING" }
        bindings = bindings - target // Required to change order
    }

    bind(target, provider)
}

inline fun <reified T : Any> Module.forceBind(instance: T) {
    forceBind(Target(T::class), Instance(instance))
}

inline fun <reified T : Any> Module.forceBind(noinline generator: () -> T) {
    forceBind(Target(T::class), Generator(generator))
}

inline fun <reified T : Any> Module.forceBind(tag: Any, instance: T) {
    forceBind(Target(T::class, tag), Instance(instance))
}

inline fun <reified T : Any> Module.forceBind(tag: Any, noinline generator: () -> T) {
    forceBind(Target(T::class, tag), Generator(generator))
}
