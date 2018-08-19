package com.hexagonkt.vertx

import io.vertx.core.Context
import io.vertx.core.Future
import io.vertx.core.Verticle as V
import io.vertx.core.Vertx as Vx

fun Vertx(block: Vertx.() -> Unit) = Vertx().apply(block)

class Vertx : Vx by Vx.vertx() {
    fun Verticle(block: Verticle.() -> Unit): V = Verticle()
}

class Verticle : V {
    override fun start(startFuture: Future<Void>?) {
        TODO("not implemented")
    }

    override fun stop(stopFuture: Future<Void>?) {
        TODO("not implemented")
    }

    override fun getVertx(): Vx {
        TODO("not implemented")
    }

    override fun init(vertx: Vx?, context: Context?) {
        TODO("not implemented")
    }

    fun HttpServer(): Nothing = TODO()
}

fun f () {
    Vertx {
        Verticle {
            HttpServer()
        }
    }
}
