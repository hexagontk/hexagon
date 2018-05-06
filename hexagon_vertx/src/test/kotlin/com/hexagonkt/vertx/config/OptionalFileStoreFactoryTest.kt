package com.hexagonkt.vertx.config

import com.hexagonkt.vertx.toJsonObject
import io.vertx.core.Vertx
import org.junit.Test

class OptionalFileStoreFactoryTest {
    @Test(expected = IllegalStateException::class)
    fun `Create 'OptionalFileStore' without path fails`() {
        OptionalFileStoreFactory().create(Vertx.vertx(), mapOf("bad" to "parameter").toJsonObject())
    }

    @Test fun `Create an 'OptionalFileStore' with that requires the file works`() {
        OptionalFileStoreFactory().create(
            Vertx.vertx(),
            mapOf(
                "path" to "/not_found",
                "allowNotFound" to false
            )
            .toJsonObject()
        )
    }
}
