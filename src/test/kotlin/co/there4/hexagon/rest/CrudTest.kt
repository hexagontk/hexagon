package co.there4.hexagon.rest

import co.there4.hexagon.repository.IdRepositoryTest
import co.there4.hexagon.web.Client
import co.there4.hexagon.web.Server
import co.there4.hexagon.web.backend.servlet.JettyServletServer
import org.testng.annotations.AfterClass
import org.testng.annotations.BeforeClass
import kotlin.reflect.KClass
import kotlin.reflect.KProperty1

abstract class CrudTest <T : Any, K : Any> (type: KClass<T>, key: KProperty1<T, K>) :
    IdRepositoryTest<T, K>(type, key) {

    val server = Server(JettyServletServer())
    val client by lazy { Client("http://${server.bindAddress.hostAddress}:${server.runtimePort}") }

    @BeforeClass fun startServer() {
        server.crud(idCollection)
        server.run()
    }

    @AfterClass fun stopServer() {
        server.stop()
    }

    fun crud_operations_behave_properly() {
//        val objects = createObjects ()
//        val changedObjects = objects.map { this.changeObject(it) }
//        val ids = objects.map { idCollection.getKey(it) }
//
//        client.post("/")
    }
}
