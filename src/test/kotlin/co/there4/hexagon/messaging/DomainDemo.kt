package co.there4.hexagon.messaging

class Rabbit {
    fun exchange (cb: Exchange.() -> Unit) {}
    fun queue (cb: Exchange.() -> Unit) {}

}

class Exchange {
    fun route(rk: String, cb: String.() -> Unit) {}
}

class Queue

fun rabbit (uri: String, cb: Rabbit.() -> Unit) {

}

fun demo () {
    rabbit (uri = "") {
        exchange {
            route ("evt.routing") {
                exchange {

                }

                queue {

                }

                handler {

                }
            }
        }
    }
}

fun handler(function: () -> Unit) {}
