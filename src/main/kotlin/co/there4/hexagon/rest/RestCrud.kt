package co.there4.hexagon.rest

import co.there4.hexagon.repository.MongoIdRepository
import co.there4.hexagon.repository.MongoRepository
import co.there4.hexagon.serialization.defaultFormat
import co.there4.hexagon.serialization.parse
import co.there4.hexagon.serialization.serialize
import co.there4.hexagon.web.*
import com.mongodb.MongoWriteException

class RestCrud <T : Any, K : Any> (
    val repository: MongoIdRepository<T, K>,
    server: Server) {

    init {
        val collectionName = repository.namespace.collectionName

        server.post("/$collectionName") { insert (repository, this) }
        server.put("/$collectionName") { replace (repository, this) }
        server.get("/$collectionName") { findAll (repository, this) }

        server.delete("/$collectionName/{id}") { delete (repository, this) }
        server.get("/$collectionName/{id}") { find (repository, this) }
    }

    private fun contentType (exchange: Exchange) = exchange.request.contentType ?: defaultFormat
    private fun accept (exchange: Exchange) = exchange.request.accept()?.first().let {
        when (it) {
            "*/*", null -> defaultFormat
            else -> it
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

    private fun <T : Any, K : Any> replace (
        repository: MongoIdRepository<T, K>, exchange: Exchange) {

        val obj = exchange.request.body.parse(repository.type, contentType(exchange))
        repository.replaceObject(obj)
        exchange.ok(200) // Created
    }

    private fun <T : Any, K : Any> delete (
        repository: MongoIdRepository<T, K>, exchange: Exchange) {

        val key = parseKey(repository, exchange)
        repository.deleteId(key)
        exchange.ok(200)
    }

    private fun <T : Any, K : Any> find (
        repository: MongoIdRepository<T, K>, exchange: Exchange) {

        val key = parseKey(repository, exchange)
        val obj = repository.find(key)

        if (obj == null) {
            exchange.halt(404)//NOT_FOUND)
        }
        else {
            exchange.ok(obj.serialize(accept(exchange)))
        }
    }

    private fun <K : Any, T : Any> parseKey(
        repository: MongoIdRepository<T, K>, exchange: Exchange): K {

        val id: String = exchange.request.parameter("id")
        return when (repository.keyType) {
            String::class -> """"$id""""
            else -> id
        }.parse(repository.keyType, contentType(exchange))
    }

    private fun <T : Any> findAll (repository: MongoRepository<T>, exchange: Exchange) {
        val objects = repository.findObjects().toList()
        exchange.ok(objects.serialize(accept(exchange)))
    }
}
