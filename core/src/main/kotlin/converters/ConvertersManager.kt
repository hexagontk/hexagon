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
     * Convert one type to another using the registered mapper function among both types. On sources
     * of the same type as [target], the source object is returned without conversion.
     *
     * Converter search *DOES NOT CONSIDER SOURCE'S INTERFACES OR PARENT CLASSES*. If no exact type
     * is registered for the converter, it won't be found. There is an exception with maps: if no
     * converter is found and source implements Map, `Map::class to KClass<Target>` will be
     * searched in the converters' registry.
     *
     * If no mapper function is defined for the specified types, an exception is thrown.
     *
     * @param source Value to convert to another type.
     * @param target Target type for the source instance.
     * @return Source converted to the target type, or source itself if its type is the same as
     *   target.
     */
    @Suppress("UNCHECKED_CAST") // Type consistency is checked at runtime
    fun <S : Any, T : Any> convert(source: S, target: KClass<T>): T =
        if (source::class == target)
            source as T
        else
            searchConverter(source, target)
                ?.invoke(source)
                ?: error("No converter for ${source::class.simpleName} -> ${target.simpleName}")

    /**
     * Convert a group of instances of one type to another type using the registered mapper function
     * among both types. If no mapper function is defined for the specified types, an exception is
     * thrown.
     *
     * @param source Values to convert to another type.
     * @param target Target type for the source instances in the group.
     * @return List of converted instances.
     *
     * @see ConvertersManager.convert
     */
    @Suppress("UNCHECKED_CAST") // Type consistency is checked at runtime
    fun <S : Any, T : Any> convertObjects(source: Iterable<S>, target: KClass<T>): List<T> =
        source.map { convert(it, target) }

    @Suppress("UNCHECKED_CAST") // Type consistency is checked at runtime
    private fun <S : Any, T : Any> searchConverter(source: S, target: KClass<T>): ((S) -> T)? {
        val sourceType = source::class

        val converter = converters[sourceType to target]
        if (converter != null)
            return converter as? (S) -> T

        if (source is Map<*, *>) {
            val superTypeConverter = converters[Map::class to target]
            if (superTypeConverter != null)
                return superTypeConverter as? (S) -> T
        }

        return null
    }
}
