package co.there4.hexagon

import co.there4.hexagon.util.Log
import co.there4.hexagon.server.*
import co.there4.hexagon.server.HttpMethod.*
import javax.script.Compilable
import javax.script.ScriptEngineManager

val getIndex = get()

class SampleRouter  {
    val s = server {
        before {
            if (request.method == POST)
                return@before

            response.addHeader("foo", "bar")
        }

        assets ("/public", "/public")

        get { "Hi" }

        getIndex by {}
        getIndex.handler {}

        post("/foo") by {}
        post("/foo").handler {}

        POST at "/foo" by { "Done" }
        POST at "/foo" by { done() }
        POST at "/foo" by this@SampleRouter::reference

        ALL at "/" before {
        }

        ALL at "/" after {
        }
    }

    private fun done(): Any = 200 to "Done"
    private fun reference(e: Exchange): Any = 200 to "Done"

    fun f() {
        s.run()
    }
}

fun main (vararg args: String) {
    val engine = ScriptEngineManager().getEngineByExtension("kts") as Compilable
    Log.time {
        val compile = engine.compile("val x = 3\nx + 2")
        Log.time {
            println(compile.eval())
        }
    }
}

val s = server {
    before {
        if (request.method == POST)
            return@before

        response.addHeader("foo", "bar")
    }

    assets ("/public", "/public")

    get { "Hi" }

    getIndex by {}
    getIndex.handler {}

    post("/foo") by {}
    post("/foo").handler {}

    POST at "/foo" by { "Done" }
    POST at "/foo" by { done() }
    POST at "/foo" by ::reference

    ALL at "/" before {
    }

    ALL at "/" after {
    }
}

private fun done(): Any = 200 to "Done"
private fun reference(e: Exchange): Any = 200 to "Done"

fun f() {
    s.run()
}
