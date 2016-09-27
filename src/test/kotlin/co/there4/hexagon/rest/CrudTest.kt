package co.there4.hexagon.rest

import co.there4.hexagon.web.server as hServer
import co.there4.hexagon.web.Server
import org.testng.annotations.Test
import kotlin.reflect.KProperty1

@Test abstract class CrudTest <T : Any, K : Any> (key: KProperty1<T, K>, server: Server = hServer) {

}
