package co.there4.hexagon.rest

import co.there4.hexagon.repository.MongoRepository
import co.there4.hexagon.serialization.*
import co.there4.hexagon.web.*
import com.mongodb.MongoWriteException
import com.mongodb.client.FindIterable
import java.nio.charset.Charset.defaultCharset

/**
 * TODO Support paging (limit and skip query parameters) in all methods
 * TODO implement GET /<Class>/ids properly
 * TODO Implement pattern find with filters made from query strings (?<fieldName>=<val1>,<val2>...&)
 * TODO Implement pattern delete with filters made from query strings (see above)
 * TODO Make implementation for MongoRepository (without IDs)
 */
class RestBaseCrud <T : Any> (
    val repository: MongoRepository<T>, server: Server, readOnly: Boolean = false) {
    init {
        val collectionName = repository.namespace.collectionName

        server.get("/$collectionName") { findAll (repository, this) }
        server.get("/$collectionName/count") { ok(repository.count()) }
        server.get("/$collectionName/ids") { ok(repository.find().toList().serialize()) }

        if (!readOnly) {
            server.post("/$collectionName/list") { insertList (repository, this) }
            server.post("/$collectionName") { insert (repository, this) }
        }
    }

    private fun contentType (exchange: Exchange) = exchange.request.contentType ?: defaultFormat
    private fun accept (exchange: Exchange) = exchange.request.accept()?.first().let {
        if (it != null && contentTypes.contains(it)) it
        else defaultFormat
    }

    private fun <T : Any> insertList (repository: MongoRepository<T>, exchange: Exchange) {
        val obj = exchange.request.body.parseList(repository.type, contentType(exchange))
        try {
            repository.insertManyObjects(obj)
            exchange.ok(201) // Created
        }
        catch (e: MongoWriteException) {
            if (e.error.code == 11000)
                exchange.halt(500)//(UNPROCESSABLE_ENTITY)
            else
                throw e
        }
    }

    private fun <T : Any> insert (repository: MongoRepository<T>, exchange: Exchange) {
        val obj = exchange.request.body.parse(repository.type, contentType(exchange))
        try {
            repository.insertOneObject(obj)
            exchange.ok(201) // Created
        }
        catch (e: MongoWriteException) {
            if (e.error.code == 11000)
                exchange.halt(500)//(UNPROCESSABLE_ENTITY)
            else
                throw e
        }
    }

    private fun <T : Any> findAll (repository: MongoRepository<T>, exchange: Exchange) {
        val objects = repository.findObjects() { pageResults(exchange) }.toList()
        val contentType = accept(exchange)
        exchange.response.contentType = contentType + "; charset=${defaultCharset().name()}"
        exchange.ok(objects.serialize(contentType))
    }

    private fun FindIterable<*>.pageResults(exchange: Exchange) {
        val limit = exchange.request["limit"]
        if (limit != null)
            limit(limit.toInt())
        val skip = exchange.request["skip"]
        if (skip != null)
            skip(skip.toInt())
    }
}
