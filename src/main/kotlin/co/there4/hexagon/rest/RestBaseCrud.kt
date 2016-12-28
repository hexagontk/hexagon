package co.there4.hexagon.rest

import co.there4.hexagon.repository.MongoRepository
import co.there4.hexagon.repository.eq
import co.there4.hexagon.repository.isIn
import co.there4.hexagon.serialization.*
import co.there4.hexagon.web.*
import com.mongodb.MongoWriteException
import com.mongodb.client.FindIterable
import com.mongodb.client.model.Filters
import org.bson.conversions.Bson
import java.nio.charset.Charset.defaultCharset
import kotlin.reflect.declaredMemberProperties

/**
 * TODO Implement pattern find with filters made from query strings (?<fieldName>=<val1>,<val2>...&)
 * TODO Implement pattern delete with filters made from query strings (see above)
 */
open class RestBaseCrud <T : Any> (
    open val repository: MongoRepository<T>,
    open val server: Server,
    open val readOnly: Boolean = false) {

    open fun install() {
        val collectionName = repository.namespace.collectionName

        server.get("/$collectionName") { findAll (repository, this) }
        server.get("/$collectionName/count") { ok(repository.count()) }

        if (!readOnly) {
            server.post("/$collectionName/list") { insertList (repository, this) }
            server.post("/$collectionName") { insert (repository, this) }
            server.delete("/$collectionName") { deleteByExample (repository, this) }
        }
    }

    protected fun contentType (exchange: Exchange) = exchange.request.contentType ?: defaultFormat
    protected fun accept (exchange: Exchange) = exchange.request.accept()?.first().let {
        if (it != null && contentTypes.contains(it)) it
        else defaultFormat
    }

    protected fun <T : Any> insertList (repository: MongoRepository<T>, exchange: Exchange) {
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

    protected fun <T : Any> insert (repository: MongoRepository<T>, exchange: Exchange) {
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

    protected fun <T : Any> findAll (repository: MongoRepository<T>, exchange: Exchange) {
        val exampleFilter = filterByExample(exchange)
        val objects =
            if (exampleFilter == null) repository.findObjects { pageResults(exchange) }.toList()
            else repository.findObjects (exampleFilter) { pageResults(exchange) }.toList()
        val contentType = accept(exchange)
        exchange.response.contentType = contentType + "; charset=${defaultCharset().name()}"
        exchange.ok(objects.serialize(contentType))
    }

    private fun deleteByExample(repository: MongoRepository<T>, exchange: Exchange) {
        val exampleFilter = filterByExample(exchange)
        if (exampleFilter == null)
            exchange.error(400, "A filter is required")
        else
            repository.deleteMany(exampleFilter)
    }

    protected fun filterByExample(exchange: Exchange): Bson? {
        val parameters = exchange.request.parameters
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

    protected fun FindIterable<*>.pageResults(exchange: Exchange): FindIterable<*> {
        val limit = exchange.request["limit"]
        if (limit != null)
            limit(limit.toInt())
        val skip = exchange.request["skip"]
        if (skip != null)
            skip(skip.toInt())

        return this
    }
}
