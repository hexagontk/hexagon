package com.hexagonkt.cucumber

import com.hexagonkt.sync
import com.hexagonkt.vertx.Device
import com.hexagonkt.vertx.devicesApplication
import com.hexagonkt.vertx.main
import cucumber.api.java.After
import cucumber.api.java.Before
import cucumber.api.java8.En
import io.vertx.core.http.HttpClient

@Suppress("unused")
class Steps : En {
    private val webClient: HttpClient = devicesApplication.vertx.createHttpClient()
    private lateinit var device: Device
    private var result: Int = 0

    @Before fun start() {
        main()
    }

    @After fun stop() = sync {
        devicesApplication.stop()
    }

    init {
        Given<String>("""a (\w+) type""") { _ ->
//            device =
//                if (deviceType == "valid") Device(1, "brand", "model", ANDROID, "version")
//                else  Device(-1, "brand", "model", ANDROID, "version")
        }

        When("a client calls the API with a POST") {
//            webClient.post("http://localhost:8080/devices/v0/devices").sendJson(device) {
//                result =
//                    if (it.succeeded()) it.result().statusCode()
//                    else 0
//            }
        }

        Then<Int>("""the service returns (\d+)""") { _ ->
//            assert(result == resultCode)
        }
    }
}
