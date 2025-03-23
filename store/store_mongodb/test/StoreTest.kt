package com.hexagontk.store.mongodb

import com.hexagontk.core.fail
import com.hexagontk.store.Store
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.TestInstance
import org.junit.jupiter.api.TestInstance.Lifecycle.PER_CLASS
import kotlin.test.assertEquals

@TestInstance(PER_CLASS)
abstract class StoreTest<T : Any, K : Any> {

    private val store: Store<T, K> by lazy {
        createStore()
    }

    protected abstract fun createStore(): Store<T, K>

    protected abstract fun createTestEntities(): List<T>

    protected abstract fun changeObject(obj: T): T

    @BeforeEach fun dropCollection() {
        store.drop()
    }

//    private val fields: List<String> by lazy {
//        createTestEntities()
//            .convertObjects(Map::class)
//            .flatMap {
//                it.keys.map { k -> k.toString() }
//            }
//    }

//    fun new_records_are_stored() {
//
//        createTestEntities().forEach { entity ->
//            store.insertOne(entity)
//            val storedEntity = store.findOne(store.key.get(entity))
//            assertEquals(entity, storedEntity)
//
//            assert(store.replaceOne(entity)) // Ensures unmodified instance is also "replaced"
//            val changedEntity = changeObject(entity)
//            assert(store.replaceOne(changedEntity))
//            val storedModifiedEntity = store.findOne(store.key.get(entity))
//            assert(storedModifiedEntity == changedEntity)
//
//            val key = store.key.get(changedEntity)
//            val fields = this.fields.toList()
//
//            val keyName = store.key.name
//            val expected = store.findOne(key, fields) ?: fail
//            val actual = store.findOne(mapOf(keyName to key), fields) ?: fail
//            store.findOne(mapOf(keyName to key), fields) ?: fail // DEBUG
//            assertTrue((expected["logo"] as? ByteArray).contentEquals(actual["logo"] as? ByteArray))
//            assertEquals(expected - "logo", actual - "logo")
//            assertEquals(store.findOne(key), store.findOne(mapOf(keyName to key)))
//
//            assertEquals(1L, store.count())
//            assert(store.deleteOne(key))
//            assertEquals(0L, store.count())
//            assertFalse(store.replaceOne(entity))
//            assertFalse(store.updateOne(key, mapOf("web" to urlOf("http://update.example.org"))))
//            assertFalse(store.deleteOne(key))
//            assertNull(store.findOne(key))
//            assertNull(store.findOne(key, listOf("web")))
//        }
//    }

//    fun many_records_are_stored() {
//        val companies: List<T> = createTestEntities()
//
//        val keys = store.insertMany(companies)
//        assert(store.count() == companies.size.toLong())
//
//        val changedCompanies = companies.map { changeObject(it) }
//        assert(store.replaceMany(changedCompanies).size == companies.size)
//
//        // TODO Improve asserts of methods below
//        checkFindAllObjects()
//        checkFindAllFields()
//        checkFindObjects()
//        checkFindFields()
//
//        assert(store.count(mapOf("id" to keys)) == companies.size.toLong())
//        assert(store.deleteMany(mapOf("id" to keys)) == keys.size.toLong())
//        assert(store.count() == 0L)
//        assert(store.replaceMany(changedCompanies).isEmpty())
//    }

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
            assertEquals(mappedClass, storedClass)
        }
    }

    private fun checkFindAllObjects() {

        val results5 = store.findAll(sort = mapOf("id" to false))
        assert(results5.size == createTestEntities().size)

        val results6 = store.findAll()
        assert(results6.size == createTestEntities().size)
    }

//    private fun checkFindAllFields() {
//        val fields = this.fields.toList()
//
//        val results5 = store.findAll(fields, sort = mapOf("id" to false))
//        assert(results5.size == createTestEntities().size)
//
//        val results6 = store.findAll(fields)
//        assert(results6.size == createTestEntities().size)
//    }

    private fun checkFindObjects() {
        val filter = emptyMap<String, Any>()

        val results5 = store.findMany(filter, sort = mapOf("id" to false))
        assert(results5.size == createTestEntities().size)

        val results6 = store.findMany(filter)
        assert(results6.size == createTestEntities().size)
    }

//    private fun checkFindFields() {
//        val fields = this.fields.toList()
//        val filter = emptyMap<String, Any>()
//
//        val results5 = store.findMany(filter, fields, sort = mapOf("id" to false))
//        assert(results5.size == createTestEntities().size)
//
//        val results6 = store.findMany(filter, fields)
//        assert(results6.size == createTestEntities().size)
//    }
}
