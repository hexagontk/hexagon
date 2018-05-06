package com.hexagonkt.vertx

import com.hexagonkt.sync
import com.hexagonkt.vertx.config.retrieveConfig
import io.vertx.core.Future
import io.vertx.core.Verticle
import io.vertx.kotlin.coroutines.await

class VertxApplication(private val supplier: () -> Verticle) {
    val vertx = createVertx()

    private lateinit var deploymentId: String

    fun start(vararg args: String) = sync {
        val config = vertx.retrieveConfig(*args).await()
        deploymentId = vertx.deployVerticle(supplier, config).await()
    }

    fun stop() = sync {
        val future = Future.future<Void>()
        vertx.undeploy(deploymentId, future)
        future.await()
    }
}
