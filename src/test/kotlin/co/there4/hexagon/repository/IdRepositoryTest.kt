package co.there4.hexagon.repository

import co.there4.hexagon.repository.MongoIdRepository
import kotlin.reflect.KClass

abstract class IdRepositoryTest <T : Any, K : Any> (
    type: KClass<T>, idField: String, keyType: KClass<K>, keySupplier: (T) -> K) :
    RepositoryTest<T, K>(type, idField) {

    fun <T : Any, K : Any> createCollection (
        type: KClass<T>,
        keyName: String,
        keyType: KClass<K>,
        keySupplier: (T) -> K) : MongoIdRepository<T, K> {

        val database = createDatabase (type)
        val collection = database.getCollection(type.simpleName)
        return MongoIdRepository(type, collection, keyName, keyType, keySupplier)
    }

    val idCollection: MongoIdRepository<T, K> =
        createCollection (type, idField, keyType, keySupplier)

    override fun getObjectKey (obj: T) = idCollection.getKey (obj)

    fun performing_crud_operations_with_lists_of_objects_behaves_as_expected () {
        val objects = createObjects ()
        val changedObjects = objects.map { this.changeObject(it) }
        val ids = objects.map { idCollection.getKey(it) }

        idCollection.insertManyObjects(objects)
        assert(ids.map { idCollection.find(it) } == objects)
        assert(idCollection.find(ids) == objects)
        ids.forEach { idCollection.deleteId(it) }
        assert(idCollection.find(ids).isEmpty())

        idCollection.insertManyObjects(objects)
        assert(idCollection.find(ids) == objects)
        objects.forEach { idCollection.deleteObject(it) }
        assert(idCollection.find(ids).isEmpty())

        idCollection.insertManyObjects(objects)
        assert(idCollection.find(ids) == objects)
        idCollection.deleteIds(ids)
        assert(idCollection.find(ids).isEmpty())

        idCollection.insertManyObjects(objects)
        assert(idCollection.find(ids) == objects)
        idCollection.deleteObjects(objects)
        assert(idCollection.find(ids).isEmpty())

        idCollection.insertManyObjects(objects)
        assert(idCollection.find(ids).size == objects.size)
        idCollection.replaceObjects(changedObjects)
        assert(idCollection.find(ids) == changedObjects)
    }
}
