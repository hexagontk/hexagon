package com.hexagonkt.vertx

import com.fasterxml.jackson.databind.ObjectWriter
import com.hexagonkt.CodedException
import com.hexagonkt.info
import com.hexagonkt.logger
import com.hexagonkt.sync
import com.hexagonkt.vertx.http.*
import com.hexagonkt.vertx.http.store.StoreController
import com.hexagonkt.vertx.store.Store
import com.hexagonkt.vertx.store.mongodb.MongoDbStore

import io.vertx.core.json.Json
import io.vertx.core.json.JsonObject
import io.vertx.ext.mongo.MongoClient
import io.vertx.ext.mongo.MongoClientDeleteResult
import io.vertx.ext.web.Router
import io.vertx.ext.web.RoutingContext
import io.vertx.kotlin.core.json.JsonObject
import io.vertx.redis.RedisClient
import io.vertx.redis.RedisOptions

import org.slf4j.Logger
import com.fasterxml.jackson.core.type.TypeReference as Type

enum class DeviceOs { ANDROID, IOS }

@Suppress("MemberVisibilityCanBePrivate")
data class Device(
    val id: String,
    val brand: String,
    val model: String,
    val os: DeviceOs,
    val osVersion: String,

    val alias: String = "$brand $model"
)

class DevicesVerticle : HttpVerticle() {
    private val log: Logger = logger()

    private val jsonMapType: Type<Map<String, *>> = object : Type<Map<String, *>>() {}

    private val writer: ObjectWriter = Json.mapper.writer()
    private val database: MongoClient by lazy { MongoClient.createShared(vertx, config()) }
    private val getDeviceProjection = JsonObject("_id" to 0)

    private val devicesStore: Store<Device, String> by lazy {
        MongoDbStore(database, Device::class, Device::id, "devices")
    }

    private val devicesController: StoreController<Device, String> by lazy {
        StoreController(devicesStore)
    }

    private val redis: RedisClient by lazy {
        RedisClient.create(vertx, RedisOptions().setHost(System.getenv("REDISHOST") ?: "127.0.0.1"))
    }

    private var map: Map<String, Device> = emptyMap()

    override fun router(): Router = router("/devices") {
        get("/types/null") { handle { null } }
        get("/types/empty") { handle { emptyList<String>() } }
        get("/types/error") { handle { throw CodedException(400, "Booooo") } }
        get("/types/object") { handle { Device("id", "brand", "model", DeviceOs.ANDROID, "1.0") } }
        post(Device::class, "/types/object") { log.info(it) }

        get("/map/:deviceId", ::getOneDeviceMap) // Map cache test
        get("/redis/:deviceId", ::getOneDeviceRedis) // Resdis test

        val serviceIdPath = "/:deviceId/applications/:applicationId/services/:serviceId"
        get(serviceIdPath, ::notImplemented)
        put(serviceIdPath, ::notImplemented)
        patch(serviceIdPath, ::notImplemented)
        delete(serviceIdPath, ::notImplemented)

        val servicesPath = "/:deviceId/applications/:applicationId/services"
        get(servicesPath, ::notImplemented)
        post(servicesPath, ::notImplemented)

        val applicationIdPath = "/:deviceId/applications/:applicationId"
        get(applicationIdPath, ::notImplemented)
        put(applicationIdPath, ::notImplemented)
        patch(applicationIdPath, ::notImplemented)
        delete(applicationIdPath, ::notImplemented)

        val applicationsPath = "/:deviceId/applications"
        get(applicationsPath, ::notImplemented)
        post(applicationsPath, ::notImplemented)

        val deviceIdPath = "/:deviceId"
        get(deviceIdPath, ::getOneDevice)
        put(deviceIdPath, ::notImplemented)
        patch(deviceIdPath, ::notImplemented)
        delete(deviceIdPath, ::notImplemented)
        post(deviceIdPath, ::postDevices)

        get(::getAllDevices)
        post(::postDevices)
        delete(::deleteDevices)
    }

    private fun getOneDeviceMap(context: RoutingContext) {
        val deviceId = context.request.getParam("deviceId")
        context.end(200, writer.writeValueAsString(map[deviceId]))
    }

    private fun getOneDevice(context: RoutingContext) {
        val deviceId = context.request.getParam("deviceId")
        val handler = context.handler<JsonObject> { end(200, it?.encode() ?: "") }
        database.findOne("devices", JsonObject("_id" to deviceId), getDeviceProjection, handler)
    }

    private fun getOneDeviceRedis(context: RoutingContext) {
        val deviceId = context.request.getParam("deviceId")
        val handler = context.handler<String> { end(200, it ?: "") }
        redis.get(deviceId, handler)
    }

    private fun getAllDevices(context: RoutingContext) {
        val handler = context.handler<JsonObject> { end(200, it?.encode() ?: "") }
        database.findOne("devices", JsonObject(), JsonObject(), handler)
    }

    private fun postDevices(context: RoutingContext) {
        context.handleList(Device::class) { devices ->
            devices.map {
                val map = Json.mapper.convertValue<Map<String, *>>(it, jsonMapType)
                map + ("_id" to map["id"])
            }
            .forEach { device ->

                val mongodbHandler = context.handler<String> {
                    map += devices.first().id to devices.first()
                    val redisHandler = context.handler<Void> { end(201, "OK") }
                    val id = device["_id"].toString()
                    redis.set(id, writer.writeValueAsString(device), redisHandler)
                }

                database.insert("devices", JsonObject(device), mongodbHandler)
            }
        }
    }

    private fun deleteDevices(context: RoutingContext) {
        val handler = context.handler<MongoClientDeleteResult> { end(200, "Deleted") }
        database.removeDocuments("devices", JsonObject(), handler)
    }
}

val devicesApplication: VertxApplication = VertxApplication(::DevicesVerticle)

fun main(vararg args: String) = sync {
    devicesApplication.start(*args)
}
