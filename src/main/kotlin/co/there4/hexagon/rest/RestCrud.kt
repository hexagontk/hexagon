package co.there4.hexagon.rest

import co.there4.hexagon.repository.MongoIdRepository
import co.there4.hexagon.repository.MongoRepository
import co.there4.hexagon.serialization.*
import co.there4.hexagon.server.*
import com.mongodb.client.model.Projections.include
import java.nio.charset.Charset.defaultCharset

/**
 * TODO Implement pattern find with filters made from query strings (?<fieldName>=<val1>,<val2>...&)
 */
fun Server.crud(repository: MongoIdRepository<*, *>) {
    this.crud(repository as MongoRepository<*>)

    val collectionName = repository.namespace.collectionName

    router.get("/$collectionName/ids") { findIds(repository) }
    router.get("/$collectionName/*,*") { findList (repository) }
    router.get("/$collectionName/{id}") { find (repository) }

    router.put("/$collectionName/list") { replaceList (repository) }
    router.put("/$collectionName") { replace (repository) }

    router.delete("/$collectionName/*,*") { deleteList (repository) }
    router.delete("/$collectionName/{id}") { delete (repository) }
}

private fun Call.findIds(repository: MongoIdRepository<*, *>) {
    val keyName = repository.key.name
    val projection = include(keyName)
    val ids = repository.find().projection(projection)
    val idValues = ids.pageResults(this).map { (it as Map<*, *>)[keyName] }
    ok(idValues.toList().serialize())
}

private fun <T : Any, K : Any> Call.replaceList (repository: MongoIdRepository<T, K>) {
    val obj = request.body.parseList(repository.type, contentType(this))
    repository.replaceObjects(obj, request.parameters.containsKey("upsert"))
    ok(200) // Created
}

private fun <T : Any, K : Any> Call.replace (repository: MongoIdRepository<T, K>) {
    val obj = request.body.parse(repository.type, contentType(this))
    repository.replaceObject(obj, request.parameters.containsKey("upsert"))
    ok(200) // Created
}

private fun <T : Any, K : Any> Call.deleteList (repository: MongoIdRepository<T, K>) {
    val key = parseKeys(repository, this)
    repository.deleteIds(key)
    ok(200)
}

private fun <T : Any, K : Any> Call.findList (repository: MongoIdRepository<T, K>) {
    val keys: List<K> = parseKeys(repository, this)
    val obj = repository.find(keys) { pageResults(this@findList) }.toList()

    if (obj.isEmpty()) {
        halt(404)//NOT_FOUND)
    }
    else {
        val contentType = accept(this)
        response.contentType = contentType + "; charset=${defaultCharset().name()}"
        ok(obj.serialize(contentType))
    }
}

private fun <T : Any, K : Any> Call.delete (repository: MongoIdRepository<T, K>) {
    val key = parseKey(repository, this)
    repository.deleteId(key)
    ok(200)
}

private fun <T : Any, K : Any> Call.find (repository: MongoIdRepository<T, K>) {
    val key = parseKey(repository, this)
    val obj = repository.find(key)

    if (obj == null) {
        halt(404)//NOT_FOUND)
    }
    else {
        val contentType = accept(this)
        response.contentType = contentType + "; charset=${defaultCharset().name()}"
        ok(obj.serialize(contentType))
    }
}

private fun <K : Any, T : Any> parseKey(
    repository: MongoIdRepository<T, K>, call: Call): K {

    val id: String = call.request.parameter("id")
    return when (repository.keyType) {
        String::class -> """"$id""""
        else -> id
    }.parse(repository.keyType, contentType(call))
}

private fun <K : Any, T : Any> parseKeys(
    repository: MongoIdRepository<T, K>, call: Call): List<K> =

    call.request.path.substringAfterLast("/").split(",").map { it.trim() }.map {
        @Suppress("UNCHECKED_CAST", "IMPLICIT_CAST_TO_ANY")
        when (repository.keyType) {
            String::class -> "$it"
            Int::class -> it.toInt()
            else -> it
        } as K
    }
