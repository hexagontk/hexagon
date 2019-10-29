package com.hexagonkt.store.hashmap

import com.hexagonkt.helpers.filterEmpty
import com.hexagonkt.store.IndexOrder
import com.hexagonkt.store.Mapper
import com.hexagonkt.store.Store
import kotlin.UnsupportedOperationException
import kotlin.reflect.KClass
import kotlin.reflect.KProperty1

class HashMapStore<T : Any, K : Any>(
    override val type: KClass<T>,
    override val key: KProperty1<T, K>,
    override val name: String = type.java.simpleName,
    private val store: HashMap<K, Map<String, Any>> = hashMapOf(),
    override val mapper: Mapper<T> = HashMapMapper(type, key)) : Store<T, K> {

    override fun createIndex(unique: Boolean, fields: Map<String, IndexOrder>): String {
        throw UnsupportedOperationException("Cannot create index on HashMap")
    }

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
        return mapper.fromStore(key.name, key.get(instance)) as K
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
            .filterEmpty()
            .forEach {
                instance[it.key] = mapper.toStore(it.key, it.value as Any)
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
        return result?.let { mapper.fromStore(result) }
    }

    override fun findOne(key: K, fields: List<String>): Map<String, *>? {
        val instance = store[key]

        return fields.map { it to instance?.get(it) }.toMap()
    }

    override fun findMany(
        filter: Map<String, *>,
        limit: Int?,
        skip: Int?,
        sort: Map<String, Boolean>
    ): List<T> {
        val filteredInstances = store.filter(filter)

        @Suppress("UNCHECKED_CAST")
        return filteredInstances
            .map { store[it]!! }
            .paginate(skip ?: 0, limit ?: filteredInstances.size)
            .sort(sort)
            .map { mapper.fromStore(it as Map<String, Any>) }
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

    private fun map(instance: T): Map<String, Any> = mapper.toStore(instance)

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
        sortedBy {
            val firstSortField = sortFields.entries.first()
            val sortingValue = it[firstSortField.key]
            @Suppress("UNCHECKED_CAST")
            if (sortingValue is Comparable<*>)
                sortingValue as? Comparable<Any>
            else
                error("Not comparable value")
        }
}
