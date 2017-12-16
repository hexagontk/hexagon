package com.hexagonkt.rest

import com.hexagonkt.server.Router
import com.hexagonkt.server.router
import com.hexagonkt.store.MongoIdRepository
import com.hexagonkt.store.MongoRepository

/**
 * TODO Implement pattern find with filters made from query strings (?<fieldName>=<val1>,<val2>...&)
 * TODO Implement pattern delete with filters made from query strings (see above)
 */
fun crud(
    repository: MongoRepository<*>,
    collectionName: String = repository.namespace.collectionName): Router =

    router {
        get("/$collectionName") { findAll (repository) }
        get("/$collectionName/count") { ok(repository.count()) }

        post("/$collectionName/list") { insertList (repository) }
        post("/$collectionName") { insert (repository) }
        delete("/$collectionName") { deleteByExample (repository) }
    }

/**
 * TODO Implement pattern find with filters made from query strings (?<fieldName>=<val1>,<val2>...&)
 */
fun crud(
    repository: MongoIdRepository<*, *>,
    collectionName: String = repository.namespace.collectionName): Router {

    val router = crud(repository as MongoRepository<*>)

    router.get("/$collectionName/ids") { findIds(repository) }
    router.get("/$collectionName/*,*") { findList (repository) }
    router.get("/$collectionName/{id}") { find (repository) }

    router.put("/$collectionName/list") { replaceList (repository) }
    router.put("/$collectionName") { replace (repository) }

    router.delete("/$collectionName/*,*") { deleteList (repository) }
    router.delete("/$collectionName/{id}") { delete (repository) }

    return router
}
