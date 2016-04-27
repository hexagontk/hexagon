package co.there4.hexagon.messaging

class Rabbit {
    fun exchange (name: String, cb: Exchange.() -> Unit) {}
    fun queue (name: String, cb: Exchange.() -> Unit) {}

}

class Exchange {
    fun route(rk: String, cb: String.() -> Unit) {}
}

class Queue {

}

fun rabbit (uri: String, cb: Rabbit.() -> Unit) {}

fun demo () {
    rabbit (uri = "") {
        queue("q") {

        }
        exchange("exchange") {
            route ("evt.routing") {
                exchange ("") {

                }

                queue ("") {

                }

                handler {

                }
            }
        }
    }
}

fun handler(function: () -> Unit) {}
