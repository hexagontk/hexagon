package co.there4.hexagon.rest

import co.there4.hexagon.repository.MongoIdRepository
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
 * TODO Make implementation for MongoRepository (without IDs)
 */
class RestCrud <T : Any, K : Any> (
    override val repository: MongoIdRepository<T, K>,
    override val server: Server,
    override val readOnly: Boolean = false) :
        RestBaseCrud<T> (repository, server, readOnly) {

    override fun install() {
        super.install()

        val collectionName = repository.namespace.collectionName

        server.get("/$collectionName/ids") { ok(repository.find().toList().serialize()) }
        server.get("/$collectionName/*,*") { findList (repository, this) }
        server.get("/$collectionName/{id}") { find (repository, this) }

        if (!readOnly) {
            server.put("/$collectionName/list") { replaceList (repository, this) }
            server.put("/$collectionName") { replace (repository, this) }

            server.delete("/$collectionName/*,*") { deleteList (repository, this) }
            server.delete("/$collectionName/{id}") { delete (repository, this) }
        }
    }

    private fun <T : Any, K : Any> replaceList (
        repository: MongoIdRepository<T, K>, exchange: Exchange) {

        val obj = exchange.request.body.parseList(repository.type, contentType(exchange))
        repository.replaceObjects(obj, exchange.request.parameters.containsKey("upsert"))
        exchange.ok(200) // Created
    }

    private fun <T : Any, K : Any> replace (
        repository: MongoIdRepository<T, K>, exchange: Exchange) {

        val obj = exchange.request.body.parse(repository.type, contentType(exchange))
        repository.replaceObject(obj, exchange.request.parameters.containsKey("upsert"))
        exchange.ok(200) // Created
    }

    private fun <T : Any, K : Any> deleteList (
        repository: MongoIdRepository<T, K>, exchange: Exchange) {

        val key = parseKeys(repository, exchange)
        repository.deleteIds(key)
        exchange.ok(200)
    }

    private fun <T : Any, K : Any> findList (
        repository: MongoIdRepository<T, K>, exchange: Exchange) {

        val keys = parseKeys(repository, exchange)
        val obj = repository.find(keys)

        if (obj.isEmpty()) {
            exchange.halt(404)//NOT_FOUND)
        }
        else {
            val contentType = accept(exchange)
            exchange.response.contentType = contentType + "; charset=${defaultCharset().name()}"
            exchange.ok(obj.serialize(contentType))
        }
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
            val contentType = accept(exchange)
            exchange.response.contentType = contentType + "; charset=${defaultCharset().name()}"
            exchange.ok(obj.serialize(contentType))
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

    private fun <K : Any, T : Any> parseKeys(
        repository: MongoIdRepository<T, K>, exchange: Exchange): List<K> =

        exchange.request.pathInfo.split(",").map { it.trim() }.map {
            when (repository.keyType) {
                String::class -> "$it"
                else -> it
            }.parse(repository.keyType, contentType(exchange))
        }
}
