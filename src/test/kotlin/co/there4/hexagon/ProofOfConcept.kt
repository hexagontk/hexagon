package co.there4.hexagon

import co.there4.hexagon.util.Log
import co.there4.hexagon.web.*
import co.there4.hexagon.web.HttpMethod.*
import javax.script.Compilable
import javax.script.ScriptEngineManager

val getIndex = get()

class SampleRouter : Router() {
    init {
        getIndex by {}
        getIndex.handler {}

        post("/foo") by {}
        post("/foo").handler {}

        POST at "/foo" by { "Done" }
        POST at "/foo" by this::done
    }

    private fun done(exchange: Exchange) = exchange.handler { 200 to "Done" }
}

fun main (vararg args: String) {
//    val s = server {
//        before {
//            response.addHeader("foo", "bar")
//        }
//
//        get { "Hi" }
//    }
//    serve {
//        before {
//            response.addHeader("foo", "bar")
//        }
//
//        get { "Hi" }
//    }

    val engine = ScriptEngineManager().getEngineByExtension("kts") as Compilable
    Log.time {
        val compile = engine.compile("val x = 3\nx + 2")
        Log.time {
            println(compile.eval())
        }
    }
}
