package com.hexagonkt.core.converters

import kotlin.reflect.KClass

/**
 * Registry that holds functions to convert from one type to another.
 *
 * @sample com.hexagonkt.core.converters.ConvertersManagerTest.usageExample
 */
object ConvertersManager {

    private var converters: Map<Pair<*, *>, (Any) -> Any> = emptyMap()

    /**
     * Register a mapping function from one type to another.
     *
     * @param key Pair which key is the source type and the value is the target type.
     * @param block Block that converts an instance of the source type to the target one.
     */
    @Suppress("UNCHECKED_CAST") // Type consistency is checked at runtime
    fun <S : Any, T : Any> register(key: Pair<KClass<S>, KClass<T>>, block: (S) -> T) {
        converters = converters + (key as Pair<*, *> to block as (Any) -> Any)
    }

    /**
     * Delete an existing mapping by its key.
     *
     * @param key Key of the mapping to be removed. No error is triggered if key doesn't exist.
     */
    fun remove(key: Pair<KClass<*>, KClass<*>>) {
        converters = converters - key
    }

    /**
     * Convert one type to another using the registered mapper function among both types. If no
     * mapper function is defined for the specified types, an exception is thrown.
     *
     * @param source Value to convert to another type.
     * @param target Target type for the source instance.
     */
    @Suppress("UNCHECKED_CAST") // Type consistency is checked at runtime
    fun <S : Any, T : Any> convert(source: S, target: KClass<T>): T =
        converters[source::class to target]
            ?.invoke(source) as? T
            ?: error("No converter for ${source::class.simpleName} -> ${target.simpleName}")
}
