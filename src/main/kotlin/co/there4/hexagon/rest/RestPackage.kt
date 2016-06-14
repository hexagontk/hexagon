package co.there4.hexagon.rest

import co.there4.hexagon.repository.MongoIdRepository
import java.net.InetAddress.getByName as address

import co.there4.hexagon.web.Server
import co.there4.hexagon.web.server

fun <T : Any, K : Any> Server.crud(repository: MongoIdRepository<T, K>) {
    RestCrud (repository, this)
}

fun <T : Any, K : Any> crud(repository: MongoIdRepository<T, K>) {
    server.crud (repository)
}
