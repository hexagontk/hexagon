package com.hexagonkt.store.hashmap

import com.hexagonkt.store.IndexOrder
import com.hexagonkt.store.Mapper
import com.hexagonkt.store.Store
import kotlin.reflect.KClass
import kotlin.reflect.KProperty1

class HashMapStore <T : Any, K : Any>(
    override val type: KClass<T>,
    override val key: KProperty1<T, K>,
    override val name: String = type.java.simpleName,
    private val store: HashMap<K, T> = hashMapOf(),
    override val mapper: Mapper<T> = HashMapMapper(type, key)) : Store<T, K> {

    override fun createIndex(unique: Boolean, fields: Map<String, IndexOrder>): String {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun insertOne(instance: T): K {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun insertMany(instances: List<T>): List<K> {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun saveOne(instance: T): K? {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun saveMany(instances: List<T>): List<K?> {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun replaceOne(instance: T): Boolean {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun replaceMany(instances: List<T>): List<T> {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun updateOne(key: K, updates: Map<String, *>): Boolean {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun updateMany(filter: Map<String, *>, updates: Map<String, *>): Long {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun deleteOne(id: K): Boolean {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun deleteMany(filter: Map<String, *>): Long {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun findOne(key: K): T? {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun findOne(key: K, fields: List<String>): Map<String, *>? {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun findMany(filter: Map<String, *>, limit: Int?, skip: Int?, sort: Map<String, Boolean>): List<T> {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun findMany(
        filter: Map<String, *>,
        fields: List<String>,
        limit: Int?,
        skip: Int?,
        sort: Map<String, Boolean>
    ): List<Map<String, *>> {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun count(filter: Map<String, *>): Long {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun drop() {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }
}
