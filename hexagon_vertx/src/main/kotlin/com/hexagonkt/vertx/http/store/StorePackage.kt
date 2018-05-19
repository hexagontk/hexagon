package com.hexagonkt.vertx.http.store

import com.hexagonkt.vertx.http.*
import io.vertx.ext.web.Router

fun <T : Any, K : Any> Router.storeRouter(controller: StoreController<T, K>) {
    val name = controller.store.name

    get("/$name/:id", controller::findOne)
    get("/$name:count", controller::count)
    get("/$name", controller::findByPattern)
    post("/$name", controller::insert)
    put("/$name", controller::replace)
    patch("/$name/:id", controller::updateOne)
    patch("/$name", controller::update)
    delete("/$name/:id", controller::deleteOne)
    delete("/$name:drop", controller::drop)
    delete("/$name", controller::deleteByPattern)
}
