package com.hexagonkt.vertx.config

import com.hexagonkt.vertx.toJsonObject
import io.vertx.core.Vertx
import io.vertx.kotlin.core.json.JsonObject
import org.junit.Test

class OptionalFileStoreFactoryTest {
    @Test(expected = IllegalStateException::class)
    fun `Create 'OptionalFileStore' without path fails`() {
        OptionalFileStoreFactory().create(Vertx.vertx(), mapOf("bad" to "parameter").toJsonObject())
    }

    @Test fun `Create an 'OptionalFileStore' which requires the file works OK`() {
        OptionalFileStoreFactory().create(
            Vertx.vertx(),
            JsonObject(
                "path" to "/not_found",
                "allowNotFound" to false
            )
        )
    }

    @Test fun `Create an 'OptionalFileStore' works OK`() {
        OptionalFileStoreFactory().create(
            Vertx.vertx(),
            JsonObject("path" to "/path")
        )
    }
}
