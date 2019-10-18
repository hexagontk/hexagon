package com.hexagonkt.store.hashmap

import com.hexagonkt.helpers.filterEmpty
import com.hexagonkt.store.IndexOrder
import com.hexagonkt.store.Mapper
import com.hexagonkt.store.Store
import kotlin.reflect.KClass
import kotlin.reflect.KProperty1

class HashMapStore <T : Any, K : Any>(
    override val type: KClass<T>,
    override val key: KProperty1<T, K>,
    override val name: String = type.java.simpleName,
    private  val store: HashMap<K, Map<String, Any>> = hashMapOf(),
    override val mapper: Mapper<T> = HashMapMapper(type, key)) : Store<T, K> {

    override fun createIndex(unique: Boolean, fields: Map<String, IndexOrder>): String {
        return "index"
    }

    override fun insertOne(instance: T): K {
        store[key.get(instance)] = map(instance)
        return key.get(instance)
    }

    override fun insertMany(instances: List<T>): List<K> {
        instances.forEach {
            store[key.get(it)] = map(it)
        }

        return instances.map{ key.get(it) }
    }

    override fun saveOne(instance: T): K? {
        if(store.containsKey(key.get(instance))) {
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

    override fun replaceOne(instance: T): Boolean {
        return store.replace(key.get(instance), map(instance)) != null
    }

    override fun replaceMany(instances: List<T>): List<T> {
        return instances.mapNotNull { if(replaceOne(it)) it else null }
    }

    override fun updateOne(key: K, updates: Map<String, *>): Boolean {
        if(!store.containsKey(key)) return false

        val instance = store[key]!!.toMutableMap()

        updates
        .filterEmpty()
        .forEach { instance[it.key] = mapper.toStore(it.key, it.value as Any) }

        return store.replace(key, instance) != null
    }

    override fun updateMany(filter: Map<String, *>, updates: Map<String, *>): Long {
        val filteredInstances = store.filter(filter)

        return filteredInstances.map { updateOne(it, updates) }.count { it }.toLong()
    }

    override fun deleteOne(id: K): Boolean {
        return store.remove(id) != null
    }

    override fun deleteMany(filter: Map<String, *>): Long {
        val filteredInstances = store.filter(filter)

        return filteredInstances.map { deleteOne(it) }.count { it }.toLong()
    }

    override fun findOne(key: K): T? {
        val result = store[key]
        return if (result == null) null else mapper.fromStore(result)
    }

    override fun findOne(key: K, fields: List<String>): Map<String, *>? {
        val instance = store[key]

        return fields.map { it to instance?.get(it) }.toMap()
    }

    override fun findMany(filter: Map<String, *>, limit: Int?, skip: Int?, sort: Map<String, Boolean>): List<T> {
        val filteredInstances = store.filter(filter)
        val result = filteredInstances.subList(skip ?: 0, filteredInstances.size)

        return result.take(limit ?: result.size).map { mapper.fromStore(store[it]!!) }
    }

    override fun findMany(
        filter: Map<String, *>,
        fields: List<String>,
        limit: Int?,
        skip: Int?,
        sort: Map<String, Boolean>
    ): List<Map<String, *>> {
        val filteredInstances = store.filter(filter)

        val result = filteredInstances.mapNotNull { findOne(it, fields) }.subList(skip ?: 0, filteredInstances.size)
        return result.take(limit ?: result.size)
    }

    override fun count(filter: Map<String, *>): Long {
        return store.filter(filter).size.toLong()
    }

    override fun drop() {
        store.clear()
    }

    private fun map(instance: T): Map<String, Any> = mapper.toStore(instance)

    private fun HashMap<K, Map<String, Any>>.filter(filter: Map<String, *>): List<K> {
        return filter {
                it.value.containsValues(filter)
            }
            .map { it.key }
    }

    private fun Map<String, Any>.containsValues(filter: Map<String, *>): Boolean {
        return filter.all {
            when (val value = it.value) {
                is List<*> -> value.contains(this[it.key])
                else -> value == this[it.key]
            }
        }
    }
}
