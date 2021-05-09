package com.hexagonkt.http.server

import com.hexagonkt.serialization.JacksonMapper
import com.hexagonkt.serialization.SerializationManager
import com.hexagonkt.serialization.convertToObject
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestInstance

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class ServerSettingsTest {

    @BeforeAll fun setUpSerializationManager() {
        SerializationManager.mapper = JacksonMapper
    }

    @Test fun `Server settings loads the proper defaults`() {
        assert(mapOf("foo" to "bar").convertToObject(ServerSettings::class) == ServerSettings())

        val serverSettings = ServerSettings(bindPort = 1234)
        assert(mapOf("bindPort" to 1234).convertToObject(ServerSettings::class) == serverSettings)
    }
}
