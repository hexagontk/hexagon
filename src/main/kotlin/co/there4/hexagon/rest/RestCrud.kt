package co.there4.hexagon.rest

import co.there4.hexagon.ratpack.KChain
import co.there4.hexagon.ratpack.KContext
import co.there4.hexagon.repository.MongoIdRepository
import co.there4.hexagon.serialization.parse
import co.there4.hexagon.serialization.serialize
import com.mongodb.MongoWriteException

class RestCrud <T : Any, K : Any> (val repository: MongoIdRepository<T, K>, val chain: KChain) {
    init {
        val collectionName = repository.namespace.collectionName

        chain.path (collectionName) {
            byMethod {
                post { insert (repository) }
                put { replace (repository) }
                get { findAll (repository) }
            }
        }
        chain.path (collectionName + "/:id") {
            byMethod {
                delete { delete (repository) }
                get { find (repository) }
            }
        }
    }

    private fun <T : Any, K : Any> KContext.insert (repository: MongoIdRepository<T, K>) {
        request.body.then {
            val obj = it.text.parse(repository.type)
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
    }

    private fun <T : Any, K : Any> KContext.replace (repository: MongoIdRepository<T, K>) {
        request.body.then {
            val obj = it.text.parse(repository.type)
            repository.replaceObject(obj)
            ok(200) // Created
        }
    }

    private fun <T : Any, K : Any> KContext.delete (repository: MongoIdRepository<T, K>) {
        val key = parseKey(repository, this)
        repository.deleteId(key)
        ok(200)
    }

    private fun <T : Any, K : Any> KContext.find (repository: MongoIdRepository<T, K>) {
        val key = parseKey(repository, this)
        val obj = repository.find(key)

        if (obj == null) {
            halt(404)//NOT_FOUND)
        }
        else {
            ok(obj.serialize())
        }
    }

    private fun <K : Any, T : Any> parseKey(
        repository: MongoIdRepository<T, K>, context: KContext): K {

        val id: String = context.pathTokens["id"] ?: throw IllegalStateException ()
        return when (repository.keyType) {
            String::class -> """"$id""""
            else -> id
        }.parse(repository.keyType)
    }

    private fun <T : Any, K : Any> KContext.findAll (repository: MongoIdRepository<T, K>) {
        val objects = repository.findObjects().toList()
        ok(objects.serialize())
    }
}
