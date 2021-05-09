package com.hexagonkt.store.mongodb

import com.hexagonkt.helpers.fail
import com.hexagonkt.serialization.Json
import com.hexagonkt.serialization.SerializationManager
import com.hexagonkt.store.Store
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.TestInstance
import org.junit.jupiter.api.TestInstance.Lifecycle.PER_CLASS
import java.net.URL
import kotlin.reflect.KProperty1

@TestInstance(PER_CLASS)
abstract class StoreTest<T : Any, K : Any> {

    protected val store: Store<T, K> by lazy {
        createStore()
    }

    protected val fields: Map<String, KProperty1<T, *>> by lazy {
        store.mapper.fields
    }

    protected abstract fun createStore(): Store<T, K>

    protected abstract fun createTestEntities(): List<T>

    protected abstract fun changeObject(obj: T): T

    @BeforeAll fun initialize() {
        SerializationManager.formats = linkedSetOf(Json)
    }

    @BeforeEach fun dropCollection() {
        store.drop()
    }

    fun new_records_are_stored() {

        createTestEntities().forEach { entity ->
            store.insertOne(entity)
            val storedEntity = store.findOne(store.key.get(entity))
            assert(storedEntity == entity)

            assert(store.replaceOne(entity)) // Ensures unmodified instance is also "replaced"
            val changedEntity = changeObject(entity)
            assert(store.replaceOne(changedEntity))
            val storedModifiedEntity = store.findOne(store.key.get(entity))
            assert(storedModifiedEntity == changedEntity)

            val key = store.key.get(changedEntity)
            val fields = this.fields.keys.toList()

            val keyName = store.key.name
            assert(store.findOne(key, fields) == store.findOne(mapOf(keyName to key), fields))
            assert(store.findOne(key) == store.findOne(mapOf(keyName to key)))

            assert(store.count() == 1L)
            assert(store.deleteOne(key))
            assert(store.count() == 0L)
            assert(!store.replaceOne(entity))
            assert(!store.updateOne(key, mapOf("web" to URL("http://update.example.org"))))
            assert(!store.deleteOne(key))
            assert(store.findOne(key) == null)
            assert(store.findOne(key, listOf("web")) == null)
        }
    }

    fun many_records_are_stored() {
        val companies: List<T> = createTestEntities()

        val keys = store.insertMany(companies)
        assert(store.count() == companies.size.toLong())

        val changedCompanies = companies.map { changeObject(it) }
        assert(store.replaceMany(changedCompanies).size == companies.size)

        // TODO Improve asserts of methods below
        checkFindAllObjects()
        checkFindAllFields()
        checkFindObjects()
        checkFindFields()

        assert(store.count(mapOf("id" to keys)) == companies.size.toLong())
        assert(store.deleteMany(mapOf("id" to keys)) == keys.size.toLong())
        assert(store.count() == 0L)
        assert(store.replaceMany(changedCompanies).isEmpty())
    }

    fun entities_are_stored() {
        val testEntities = createTestEntities()

        val keys = store.saveMany(testEntities)

        if (keys.any { it == null  })
            fail

        val entities = keys.filterNotNull().mapNotNull { store.findOne(it) }

        assert(entities.map { store.key.get(it) }.all { it in keys })

        store.saveMany(testEntities.map { changeObject(it) })
    }

    fun insert_one_record_returns_the_proper_key() {

        createTestEntities().forEach { mappedClass ->
            val await = store.insertOne(mappedClass)
            val storedClass = store.findOne(await)
            assert(mappedClass == storedClass)
        }
    }

    private fun checkFindAllObjects() {

        val results5 = store.findAll(sort = mapOf("id" to false))
        assert(results5.size == createTestEntities().size)

        val results6 = store.findAll()
        assert(results6.size == createTestEntities().size)
    }

    private fun checkFindAllFields() {
        val fields = this.fields.keys.toList()

        val results5 = store.findAll(fields, sort = mapOf("id" to false))
        assert(results5.size == createTestEntities().size)

        val results6 = store.findAll(fields)
        assert(results6.size == createTestEntities().size)
    }

    private fun checkFindObjects() {
        val filter = emptyMap<String, Any>()

        val results5 = store.findMany(filter, sort = mapOf("id" to false))
        assert(results5.size == createTestEntities().size)

        val results6 = store.findMany(filter)
        assert(results6.size == createTestEntities().size)
    }

    private fun checkFindFields() {
        val fields = this.fields.keys.toList()
        val filter = emptyMap<String, Any>()

        val results5 = store.findMany(filter, fields, sort = mapOf("id" to false))
        assert(results5.size == createTestEntities().size)

        val results6 = store.findMany(filter, fields)
        assert(results6.size == createTestEntities().size)
    }
}
