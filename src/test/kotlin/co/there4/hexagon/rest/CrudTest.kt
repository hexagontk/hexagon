package co.there4.hexagon.rest

import co.there4.hexagon.repository.IdRepositoryTest
import co.there4.hexagon.web.Client
import co.there4.hexagon.web.jetty.JettyServer
import org.testng.annotations.AfterClass
import org.testng.annotations.BeforeClass
import org.testng.annotations.Test
import kotlin.reflect.KClass
import kotlin.reflect.KProperty1

@Test abstract class CrudTest <T : Any, K : Any> (type: KClass<T>, key: KProperty1<T, K>) :
    IdRepositoryTest<T, K>(type, key) {

    val server = JettyServer(bindPort = 2070)
    val client = Client("http://${server.bindAddress.hostAddress}:${server.bindPort}")

    @BeforeClass fun startServer() {
        server.crud(idCollection)
        server.run()
    }

    @AfterClass fun stopServer() {
        server.stop()
    }

    fun crud_operations_behave_properly() {
        val objects = createObjects ()
        val changedObjects = objects.map { this.changeObject(it) }
        val ids = objects.map { idCollection.getKey(it) }

//        assert (ids.all { it.javaClass == idCollection.keyType.java })
//
//        idCollection.insertManyObjects(objects)
//        assert(ids.map { idCollection.find(it) } == objects)
//        assert(idCollection.find(*idsArray) == objects)
//        ids.forEach { idCollection.deleteId(it) }
//        assert(idCollection.find(*idsArray).isEmpty())
//
//        idCollection.insertManyObjects(objects)
//        assert(idCollection.find(*idsArray) == objects)
//        objects.forEach { idCollection.deleteObject(it) }
//        assert(idCollection.find(*idsArray).isEmpty())
//
//        idCollection.insertManyObjects(objects)
//        assert(idCollection.find(*idsArray) == objects)
//        idCollection.deleteIds(*idsArray)
//        assert(idCollection.find(*idsArray).isEmpty())
//
//        idCollection.insertManyObjects(objects)
//        assert(idCollection.find(*idsArray) == objects)
//        idCollection.deleteObjects(*objectsArray)
//        assert(idCollection.find(*idsArray).isEmpty())
//
//        idCollection.insertManyObjects(objects)
//        assert(idCollection.find(*idsArray).size == objects.size)
//        idCollection.replaceObjects(*changedObjectsArray)
//        assert(idCollection.find(*idsArray) == changedObjects)
    }
}
