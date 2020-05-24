package com.hexagonkt.http.server

import com.hexagonkt.serialization.convertToObject
import org.junit.jupiter.api.Test

class ServerSettingsTest {

    @Test fun `Server settings loads the proper defaults`() {
        assert(mapOf("foo" to "bar").convertToObject(ServerSettings::class) == ServerSettings())

        val serverSettings = ServerSettings(bindPort = 1234)
        assert(mapOf("bindPort" to 1234).convertToObject(ServerSettings::class) == serverSettings)
    }
}
