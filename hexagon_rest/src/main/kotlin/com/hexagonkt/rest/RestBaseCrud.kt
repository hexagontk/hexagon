package com.hexagonkt.rest

import com.hexagonkt.store.MongoRepository
import com.hexagonkt.store.eq
import com.hexagonkt.store.isIn
import com.hexagonkt.serialization.*
import com.hexagonkt.serialization.SerializationManager.defaultFormat
import com.hexagonkt.serialization.SerializationManager.contentTypes
import com.hexagonkt.server.Call
import com.hexagonkt.server.Router
import com.mongodb.MongoWriteException
import com.mongodb.client.FindIterable
import com.mongodb.client.model.Filters
import org.bson.conversions.Bson
import java.nio.charset.Charset.defaultCharset
import kotlin.reflect.full.declaredMemberProperties

/**
 * TODO Implement pattern find with filters made from query strings (?<fieldName>=<val1>,<val2>...&)
 * TODO Implement pattern delete with filters made from query strings (see above)
 */
fun Router.crud(repository: MongoRepository<*>) {
    val collectionName = repository.namespace.collectionName

    get("/$collectionName") { findAll (repository) }
    get("/$collectionName/count") { ok(repository.count()) }

    post("/$collectionName/list") { insertList (repository) }
    post("/$collectionName") { insert (repository) }
    delete("/$collectionName") { deleteByExample (repository) }
}

internal fun accept (call: Call): SerializationFormat = call.request.accept()?.first().let {
    if (it != null && contentTypes.contains(it)) SerializationManager.getContentTypeFormat(it)
    else defaultFormat
}

internal fun <T : Any> Call.insertList (repository: MongoRepository<T>) {
    val obj = request.body.parseList(repository.type, serializationFormat())
    try {
        repository.insertManyObjects(obj)
        ok(201) // Created
    }
    catch (e: MongoWriteException) {
        if (e.error.code == 11000)
            halt(500)//(UNPROCESSABLE_ENTITY)
        else
            throw e
    }
}

internal fun <T : Any> Call.insert (repository: MongoRepository<T>) {
    val obj = request.body.parse(repository.type, serializationFormat())
    try {
        repository.insertOneObject(obj)
        ok(201) // Created
    }
    catch (e: MongoWriteException) {
        if (e.error.code == 11000)
            halt(500)//(UNPROCESSABLE_ENTITY)
        else
            throw e
    }
}

internal fun <T : Any> Call.findAll (repository: MongoRepository<T>) {
    val exampleFilter = filterByExample(repository, this)
    val objects =
        if (exampleFilter == null) repository.findObjects { pageResults(this@findAll) }.toList()
        else repository.findObjects (exampleFilter) { pageResults(this@findAll) }.toList()
    val contentType = accept(this)
    response.contentType = contentType.contentType + "; charset=${defaultCharset().name()}"
    ok(objects.serialize(contentType))
}

private fun Call.deleteByExample(repository: MongoRepository<*>) {
    val exampleFilter = filterByExample(repository, this)
    if (exampleFilter == null)
        error(400, "A filter is required")
    else
        repository.deleteMany(exampleFilter)
}

internal fun filterByExample(repository: MongoRepository<*>, call: Call): Bson? {
    val parameters = call.request.parameters
    val filters = parameters
        .filterKeys { it in repository.type.declaredMemberProperties.map { it.name } }

    return if (filters.isNotEmpty())
        Filters.and(
            filters.map {
                val value = it.value.first()
                if (value.contains(','))
                    it.key isIn value.split(',')
                else
                    it.key eq value
            }
        )
    else null
}

internal fun FindIterable<*>.pageResults(call: Call): FindIterable<*> {
    val limit = call.request["limit"]
    if (limit != null)
        limit(limit.toInt())
    val skip = call.request["skip"]
    if (skip != null)
        skip(skip.toInt())

    return this
}
