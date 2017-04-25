package co.there4.hexagon

import co.there4.hexagon.web.*
import co.there4.hexagon.web.HttpMethod.*

val getIndex = get()

class SampleRouter : Router() {
    init {
        getIndex by {}
        getIndex.handler {}

        post("/foo") by {}
        post("/foo").handler {

        }

        POST at "/foo" by {
            ok("Done")
        }

        POST at "/foo" by this::done
    }

    private fun done(exchange: Exchange): Unit = exchange.handler {
        ok("Done")
    }
}

