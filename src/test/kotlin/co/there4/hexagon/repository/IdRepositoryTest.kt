package co.there4.hexagon.repository

import kotlin.reflect.KClass

/**
 * TODO Check events
 */
abstract class IdRepositoryTest <T : Any, K : Any> (
    type: KClass<T>, idField: String, keyType: KClass<K>, keySupplier: (T) -> K) :
    RepositoryTest<T, K>(type, idField) {

    val idCollection: MongoIdRepository<T, K> =
        MongoIdRepository(type, mongoDatabase(), idField, keyType, keySupplier, true)

    override fun getObjectKey (obj: T) = idCollection.getKey (obj)

    fun performing_crud_operations_with_lists_of_objects_behaves_as_expected () {
        val objects = createObjects ()
        @Suppress("CAST_NEVER_SUCCEEDS") // It seems the only way to convert to generic array
        val objectsArray = createObjects ().toTypedArray<Any>() as Array<T>
        val changedObjects = objects.map { this.changeObject(it) }
        @Suppress("CAST_NEVER_SUCCEEDS") // It seems the only way to convert to generic array
        val changedObjectsArray = changedObjects.toTypedArray<Any>() as Array<T>
        val ids = objects.map { idCollection.getKey(it) }
        @Suppress("CAST_NEVER_SUCCEEDS") // It seems the only way to convert to generic array
        val idsArray: Array<K> = ids.toTypedArray<Any>() as Array<K>

        assert (ids.all { it.javaClass == idCollection.keyType.java })

        idCollection.insertManyObjects(objects)
        assert(ids.map { idCollection.find(it) } == objects)
        assert(idCollection.find(*idsArray) == objects)
        ids.forEach { idCollection.deleteId(it) }
        assert(idCollection.find(*idsArray).isEmpty())

        idCollection.insertManyObjects(objects)
        assert(idCollection.find(*idsArray) == objects)
        objects.forEach { idCollection.deleteObject(it) }
        assert(idCollection.find(*idsArray).isEmpty())

        idCollection.insertManyObjects(objects)
        assert(idCollection.find(*idsArray) == objects)
        idCollection.deleteIds(*idsArray)
        assert(idCollection.find(*idsArray).isEmpty())

        idCollection.insertManyObjects(objects)
        assert(idCollection.find(*idsArray) == objects)
        idCollection.deleteObjects(*objectsArray)
        assert(idCollection.find(*idsArray).isEmpty())

        idCollection.insertManyObjects(objects)
        assert(idCollection.find(*idsArray).size == objects.size)
        idCollection.replaceObjects(*changedObjectsArray)
        assert(idCollection.find(*idsArray) == changedObjects)
    }
}
