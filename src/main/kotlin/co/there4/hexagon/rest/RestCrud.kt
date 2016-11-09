package co.there4.hexagon.rest

import co.there4.hexagon.repository.MongoIdRepository
import co.there4.hexagon.repository.MongoRepository
import co.there4.hexagon.serialization.*
import co.there4.hexagon.web.*
import com.mongodb.MongoWriteException
import com.mongodb.client.FindIterable
import com.mongodb.client.model.Projections
import com.mongodb.client.model.Projections.include
import java.nio.charset.Charset.defaultCharset

/**
 * TODO Implement pattern find with filters made from query strings (?<fieldName>=<val1>,<val2>...&)
 */
class RestCrud <T : Any, K : Any> (
    override val repository: MongoIdRepository<T, K>,
    override val server: Server,
    override val readOnly: Boolean = false) :
        RestBaseCrud<T> (repository, server, readOnly) {

    override fun install() {
        super.install()

        val collectionName = repository.namespace.collectionName

        server.get("/$collectionName/ids") { findIds(repository, this) }
        server.get("/$collectionName/*,*") { findList (repository, this) }
        server.get("/$collectionName/{id}") { find (repository, this) }

        if (!readOnly) {
            server.put("/$collectionName/list") { replaceList (repository, this) }
            server.put("/$collectionName") { replace (repository, this) }

            server.delete("/$collectionName/*,*") { deleteList (repository, this) }
            server.delete("/$collectionName/{id}") { delete (repository, this) }
        }
    }

    private fun findIds(repository: MongoIdRepository<T, K>, exchange: Exchange) {
        val keyName = repository.key.name
        val projection = include(keyName)
        val ids = repository.find().projection(projection)
        val idValues = ids.pageResults(exchange).map { (it as Map<*, *>)[keyName] }
        exchange.ok(idValues.toList().serialize())
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

        val keys: List<K> = parseKeys(repository, exchange)
        val obj = repository.find(keys) { pageResults(exchange) }.toList()

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

        exchange.request.pathInfo.substringAfterLast("/").split(",").map { it.trim() }.map {
            @Suppress("UNCHECKED_CAST", "IMPLICIT_CAST_TO_ANY")
            when (repository.keyType) {
                String::class -> "$it"
                Int::class -> it.toInt()
                else -> it
            } as K
        }
}
