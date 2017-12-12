package com.hexagonkt.store

import kotlin.reflect.KClass
import kotlin.reflect.KProperty1

/**
 * TODO Check events
 */
abstract class ObjectIdRepositoryTest <T : Any> (type: KClass<T>, key: KProperty1<T, String>) :
    RepositoryTest<T, String>(type, key) {

    val oidCollection: MongoObjectIdRepository<T> = MongoObjectIdRepository(type, key)

    override fun setObjectKey (obj: T, id: Int): T = error("ObjectId should not be changed in test")
    override fun getObjectKey (obj: T) = oidCollection.getKey (obj)

    override fun createObjects() = (0..9).map { changeObject(createObject()) }

    @Suppress("unused")
    fun performing_crud_operations_with_lists_of_objects_behaves_as_expected () {
        val objects: List<T> = createObjects ()
        @Suppress("UNCHECKED_CAST") // It seems the only way to convert to generic array
        val objectsArray = objects.toTypedArray<Any>() as Array<T>
        val changedObjects = objects.map { this.changeObject(it) }
        @Suppress("UNCHECKED_CAST") // It seems the only way to convert to generic array
        val changedObjectsArray = changedObjects.toTypedArray<Any>() as Array<T>
        val ids = objects.map { oidCollection.getKey(it) }
        @Suppress("UNCHECKED_CAST") // It seems the only way to convert to generic array
        val idsArray: Array<String> = ids.toTypedArray()

        assert (ids.all { it.javaClass == oidCollection.keyType.java })

        oidCollection.insertManyObjects(objects)
        assert(ids.map { oidCollection.find(it) } == objects)
        assert(oidCollection.find(*idsArray) == objects)
        ids.forEach { oidCollection.deleteId(it) }
        assert(oidCollection.find(*idsArray).isEmpty())

        oidCollection.insertManyObjects(objects)
        assert(oidCollection.find(*idsArray) == objects)
        objects.forEach { oidCollection.deleteObject(it) }
        assert(oidCollection.find(*idsArray).isEmpty())

        oidCollection.insertManyObjects(objects)
        assert(oidCollection.find(*idsArray) == objects)
        oidCollection.deleteIds(*idsArray)
        assert(oidCollection.find(*idsArray).isEmpty())

        oidCollection.insertManyObjects(objects)
        assert(oidCollection.find(*idsArray) == objects)
        oidCollection.deleteObjects(*objectsArray)
        assert(oidCollection.find(*idsArray).isEmpty())

        oidCollection.insertManyObjects(objects)
        assert(oidCollection.find(*idsArray).size == objects.size)
        oidCollection.replaceObjects(*changedObjectsArray)
        assert(oidCollection.find(*idsArray) == changedObjects)
    }
}
