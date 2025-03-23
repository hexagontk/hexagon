package com.hexagontk.store.hashmap

import com.hexagontk.core.filterNotEmpty
import com.hexagontk.store.Store
import kotlin.reflect.KClass
import kotlin.reflect.KProperty1

class HashMapStore<T : Any, K : Any>(
    override val type: KClass<T>,
    override val key: KProperty1<T, K>,
    override val name: String = type.java.simpleName,
    private val store: HashMap<K, Map<String, Any>> = hashMapOf(),
    override val encoder: (T) -> Map<String, *>,
    override val decoder: (Map<String, *>) -> T,
) : Store<T, K> {

    override fun insertOne(instance: T): K {
        store[key.get(instance)] = map(instance)
        return key.get(instance)
    }

    override fun insertMany(instances: List<T>): List<K> {
        instances.forEach {
            store[key.get(it)] = map(it)
        }

        return instances.map { key.get(it) }
    }

    override fun saveOne(instance: T): K? {
        if (store.containsKey(key.get(instance))) {
            store[key.get(instance)] = map(instance)
            return null
        }

        store[key.get(instance)] = map(instance)
        @Suppress("UNCHECKED_CAST")
        return fromStore(key.get(instance)) as K
    }

    override fun saveMany(instances: List<T>): List<K?> {
        return instances.map(::saveOne)
    }

    override fun replaceOne(instance: T): Boolean =
        store.replace(key.get(instance), map(instance)) != null


    override fun replaceMany(instances: List<T>): List<T> =
        instances.mapNotNull { if (replaceOne(it)) it else null }


    override fun updateOne(key: K, updates: Map<String, *>): Boolean {
        if (!store.containsKey(key)) return false

        val instance = store[key]!!.toMutableMap()

        updates
            .filterNotEmpty()
            .forEach {
                instance[it.key] = toStore(it.value)
            }

        return store.replace(key, instance) != null
    }

    override fun updateMany(filter: Map<String, *>, updates: Map<String, *>): Long {
        val filteredInstances = store.filter(filter)

        return filteredInstances.map { updateOne(it, updates) }.count { it }.toLong()
    }

    override fun deleteOne(id: K): Boolean =
        store.remove(id) != null

    override fun deleteMany(filter: Map<String, *>): Long {
        val filteredInstances = store.filter(filter)

        return filteredInstances.map { deleteOne(it) }.count { it }.toLong()
    }

    override fun findOne(key: K): T? {
        val result = store[key]
        return result?.let { fromStore(result) }
    }

    override fun findOne(key: K, fields: List<String>): Map<String, *>? {
        val instance = store[key]

        return if (instance == null) null else fields.associateWith { instance[it] }
    }

    override fun findMany(
        filter: Map<String, *>,
        limit: Int?,
        skip: Int?,
        sort: Map<String, Boolean>
    ): List<T> {
        val filteredKeys = store.filter(filter)
        val filteredInstances = filteredKeys.map { store[it]!! }

        @Suppress("UNCHECKED_CAST")
        return filteredInstances
            .sort(sort)
            .paginate(skip ?: 0, limit ?: filteredKeys.size)
            .map { fromStore(it as Map<String, Any>) }
    }

    override fun findMany(
        filter: Map<String, *>,
        fields: List<String>,
        limit: Int?,
        skip: Int?,
        sort: Map<String, Boolean>
    ): List<Map<String, *>> {
        val filteredInstances = store.filter(filter)
        val result = filteredInstances.mapNotNull { findOne(it, fields) }

        return result
            .paginate(skip ?: 0, limit ?: result.size)
            .sort(sort)
    }

    override fun count(filter: Map<String, *>): Long =
        store.filter(filter).size.toLong()

    override fun drop() =
        store.clear()

    private fun map(instance: T): Map<String, Any> = toStore(instance)

    private fun HashMap<K, Map<String, Any>>.filter(filter: Map<String, *>): List<K> =
        filter { it.value.containsValues(filter) }
            .map { it.key }

    private fun Map<String, Any>.containsValues(filter: Map<String, *>): Boolean =
        filter.all {
            when (val value = it.value) {
                is List<*> -> value.contains(this[it.key])
                else -> value == this[it.key]
            }
        }

    private fun List<Map<String, *>>.paginate(skip: Int, limit: Int): List<Map<String, *>> =
        let {
            var endIndex = skip + limit
            if (endIndex > this.size) endIndex = this.size

            this.subList(skip, endIndex)
        }

    // TODO: Add sorting functionality (now only sorts by first field)
    private fun List<Map<String, *>>.sort(sortFields: Map<String, Boolean>): List<Map<String, *>> =
        if (sortFields.isEmpty())
            this
        else
            sortedBy {
                val firstSortField = sortFields.entries.first()
                val sortingValue = it[firstSortField.key]
                @Suppress("UNCHECKED_CAST")
                if (sortingValue is Comparable<*>)
                    sortingValue as? Comparable<Any>
                else
                    error("Not comparable value")
            }

    private fun toStore(instance: T): Map<String, Any>  =
        encoder(instance)
            .filterNotEmpty()
            .mapKeys { it.key.toString() }
            .mapValues { it.value }

    private fun toStore(value: Any): Any = value

    private fun fromStore(map: Map<String, Any>): T =
        decoder(map.filterNotEmpty())

    private fun fromStore(value: Any): Any =
        value
}
