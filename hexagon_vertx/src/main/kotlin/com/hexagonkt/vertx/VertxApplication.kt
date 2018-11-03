package com.hexagonkt.vertx

import com.hexagonkt.helpers.sync
import com.hexagonkt.settings.SettingsManager
import io.vertx.core.Future
import io.vertx.core.Verticle
import io.vertx.core.json.JsonObject
import io.vertx.kotlin.coroutines.await

class VertxApplication(private val supplier: () -> Verticle) {
    val vertx = createVertx()

    private lateinit var deploymentId: String

    fun start(vararg args: String) = sync {
        deploymentId = vertx.deployVerticle(supplier, JsonObject(SettingsManager.settings)).await()
    }

    fun stop() = sync {
        val future = Future.future<Void>()
        vertx.undeploy(deploymentId, future)
        future.await()
    }
}
