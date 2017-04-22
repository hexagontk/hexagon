package co.there4.hexagon

import co.there4.hexagon.web.*
import co.there4.hexagon.web.HttpMethod.*

val getIndex = get()

class SampleRouter : Router() {
    init {
        on(getIndex) {
            ok("good")
        }

        getIndex by {}
        getIndex.handler {}

        post("/foo") by {}
        post("/foo").handler {

        }

        post("/foo") + {}

        POST at "/foo" by {
            ok("Done")
        }

        POST at "/foo" by this::done
        POST at "/foo" by { done(this) }
    }

    private fun done(exchange: Exchange) {
        exchange.ok("Done")
    }
}

infix fun HttpMethod.at(path: String) = Route(Path(path), this)

fun Router.f() {
    GET at "/" by { ok("Hi!") }
}

