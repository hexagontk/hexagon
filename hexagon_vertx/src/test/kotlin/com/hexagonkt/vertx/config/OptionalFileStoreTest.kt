package com.hexagonkt.vertx.config

import com.hexagonkt.helpers.sync
import io.vertx.config.ConfigRetriever
import io.vertx.core.Future.future
import io.vertx.kotlin.config.ConfigRetrieverOptions
import io.vertx.core.Vertx
import io.vertx.core.json.JsonObject
import io.vertx.kotlin.coroutines.await
import org.junit.Test

class OptionalFileStoreTest {
    @Test fun `Existing config file is loaded`() = sync {
        val vertx = Vertx.vertx()
        val stores = listOf(optionalFileStoreOptions("data/tag.yaml"))
        val retriever = ConfigRetriever.create(vertx, ConfigRetrieverOptions(stores = stores))
        val f = future<JsonObject>()
        retriever.getConfig (f)
        f.setHandler {
            assert(it.result().getString("name") == "bar")
        }
        f.await()
    }

    @Test fun `Optional non existing config file does not fail`() = sync {
        val vertx = Vertx.vertx()
        val stores = listOf(optionalFileStoreOptions("data/invalid.yaml", allowNotFound = true))
        val retriever = ConfigRetriever.create(vertx, ConfigRetrieverOptions(stores = stores))
        val f = future<JsonObject>()
        retriever.getConfig (f)
        f.setHandler {
            assert(it.result().getString("name") == null)
        }
        f.await()
    }

    @Test fun `Required non existing config file throws an error`() = sync {
        val vertx = Vertx.vertx()
        val stores = listOf(optionalFileStoreOptions("data/invalid.yaml", allowNotFound = false))
        val retriever = ConfigRetriever.create(vertx, ConfigRetrieverOptions(stores = stores))
        val f = future<JsonObject>()
        retriever.getConfig (f)
        assert(f.failed())
    }
}
