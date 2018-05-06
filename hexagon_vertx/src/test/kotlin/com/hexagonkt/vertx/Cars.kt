package com.hexagonkt.vertx

import com.hexagonkt.logger
import com.hexagonkt.vertx.http.HttpVerticle
import com.hexagonkt.vertx.http.store.StoreController
import com.hexagonkt.vertx.http.store.storeRouter
import com.hexagonkt.vertx.store.mongodb.MongoDbStore
import io.vertx.ext.mongo.MongoClient
import io.vertx.ext.web.Router
import org.slf4j.Logger
import java.net.URL
import java.time.LocalDate

// MODEL ///////////////////////////////////////////////////////////////////////////////////////////

// Land motor vehicle
class Vehicle (
    val brand: String, // Also called 'make'. Ie: Honda
    val model: String, // Ie: Civic
    val year: Int,
    val market: String, // Ie: Asia, America, Europe

    val version: String, // Also called 'trim'. Ie: Type R

    val productionEnd: Int,

    val body: Body,
    val engines: List<VehicleEngine>,
    val transmission: Transmission,
    val driveTrain: String,
    val axles: List<Axle>,

    val maxSpeed: Int,
    val reviews: List<URL>,
    val photos: List<URL>
)

class Brand(
    val name: String,
    val owner: String, // Ie: Buggati -> Volkswagen
    val foundation: LocalDate
)

data class VehicleEngine(
    val position: EnginePosition,
    val engine: Engine
)

class EnginePosition

enum class FuelType

class Engine(val fuelType: FuelType, val valves: Int)
class Transmission
class Axle(val wheels: Int, val wheelKind: Wheel, val powered: Boolean, val differential: String)
class Wheel

enum class BodyType {
    COMPACT, SEDAN, SUV, TRUCK, PICKUP, COUPE
}

class Body( // dimensions
    val width: Int,
    val height: Int,
    val length: Int,
    val type: BodyType,
    val doors: Int,
    val seats: ClosedRange<Int>,
    val fuelTank: Int,
    val trunk: Int
)

class Option

// HTTP ////////////////////////////////////////////////////////////////////////////////////////////
class CarsVerticle : HttpVerticle() {
    private val log: Logger = logger()

    override fun router(): Router = router("/cars") {
        val database = MongoClient.createShared(vertx, config())
        val vehiclesStore = MongoDbStore(database, Vehicle::class, Vehicle::brand, "vehicles")
        val vehicleController = StoreController(vehiclesStore)

        storeRouter(vehicleController)

        log.info("Router loaded")
    }
}

// MAIN ////////////////////////////////////////////////////////////////////////////////////////////
val carsApplication = VertxApplication(::CarsVerticle)
