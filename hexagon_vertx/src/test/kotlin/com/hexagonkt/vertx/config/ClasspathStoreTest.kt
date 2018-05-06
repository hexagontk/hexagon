package com.hexagonkt.vertx.config

import org.junit.Test

class ClasspathStoreTest {
    @Test(expected = IllegalArgumentException::class)
    fun `Invalid path fails ClasspathStore creation`() { ClasspathStore("/foo", true) }
}
