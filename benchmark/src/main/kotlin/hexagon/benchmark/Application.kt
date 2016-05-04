package hexagon.benchmark

import co.there4.hexagon.ratpack.KContext
import co.there4.hexagon.rest.applicationStart
import co.there4.hexagon.serialization.serialize
import ratpack.server.BaseDir
import java.util.Properties;

internal data class Message (val message: String = "Hello, World!")
internal data class Fortune (val id: Int, val message: String)
internal data class World (val id: Int, val randomNumber: Int)

internal class Application {
    companion object {
        val SETTINGS_RESOURCE = "/server.properties"
        val DB_ROWS = 10000

        val MESSAGE = "Hello, World!"
        val CONTENT_TYPE_TEXT = "text/plain"
        val CONTENT_TYPE_JSON = "application/json"
        val QUERIES_PARAM = "queries"

        val repository = MongoDbRepository (loadConfiguration ())

        fun loadConfiguration (): Properties {
            try {
                val settings = Properties ()
                settings.load (Application::class.java.getResourceAsStream (SETTINGS_RESOURCE))
                return settings
            }
            catch (ex: Exception) {
                throw RuntimeException (ex)
            }
        }
    }

//    private fun KContext.getDb () {
//        try {
//            val worlds = repository.getWorlds (getQueries (), false)
//            response.contentType (CONTENT_TYPE_JSON)
//            ok ((if (request.queryParams [QUERIES_PARAM] == null) worlds[0] else worlds).serialize())
//        }
//        catch (e: Exception) {
//            e.printStackTrace ()
//            halt (e.message ?: "")
//        }
//    }

//    private fun KContext.getFortunes (): Object {
//        try {
//            List<Fortune> fortunes = repository.getFortunes ()
//            fortunes.add (new Fortune (0, "Additional fortune added at request time."))
//            fortunes.sort ((a, b) -> a.message.compareTo (b.message))
//
//            it.response.type ("text/html; charset=utf-8")
//            return renderMustache ("fortunes.html", fortunes)
//        }
//        catch (e: Exception) {
//            e.printStackTrace ()
//            return e.getMessage ()
//        }
//    }
//
//    private fun KContext.getUpdates (): Object {
//        try {
//            World[] worlds = repository.getWorlds (getQueries (it), true)
//            it.response.type (CONTENT_TYPE_JSON)
//            return toJson (it.queryParams (QUERIES_PARAM) == null? worlds[0] : worlds)
//        }
//        catch (e: Exception) {
//            e.printStackTrace ()
//            return e.getMessage ()
//        }
//    }

//    private fun KContext.getQueries (): Object {
//        try {
//            val parameter = request.queryParams (QUERIES_PARAM)
//            if (parameter == null)
//                return 1
//
//            int queries = parseInt (parameter);
//            if (queries < 1)
//                return 1
//            if (queries > 500)
//                return 500
//
//            return queries
//        }
//        catch (NumberFormatException ex) {
//            return 1
//        }
//    }

//    private KContext.getPlaintext (): Object {
//        it.response.type (CONTENT_TYPE_TEXT)
//        return MESSAGE
//    }
//
//    private KContext.getJson (): Object {
//        it.response.type (CONTENT_TYPE_JSON)
//        return toJson (new Message ())
//    }
//
//    private void addCommonHeaders () {
//        it.header ("Server", "Undertow/1.1.2")
//        it.response.addDateHeader ("Date", new Date ().getTime ())
//    }
//
    init {
        applicationStart {
            serverConfig {
                val settings = loadConfiguration ()
                port(settings.getProperty ("web.port").toInt())
//                bind (settings.getProperty ("web.host"))
                baseDir(BaseDir.find("logback-test.xml"))
            }

            handlers {
                get ("/json") {}//, this::getJson)
                get ("/db") {}//, this::getDb)
                get ("/query") {}//, this::getDb)
                get ("/fortune") {}//, this::getFortunes)
                get ("/update") {}//, this::getUpdates)
                get ("/plaintext") {}//, this::getPlaintext)
                //        after (this::addCommonHeaders);
            }
        }
    }
}

fun main (args: Array<String>) {
    Application ()
}
