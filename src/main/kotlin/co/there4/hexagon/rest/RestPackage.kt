package co.there4.hexagon.rest

import co.there4.hexagon.repository.FileRepository
import co.there4.hexagon.repository.MongoIdRepository
import co.there4.hexagon.repository.MongoRepository
import co.there4.hexagon.web.Exchange
import co.there4.hexagon.web.Server
import co.there4.hexagon.web.server
import java.net.InetAddress.getByName as address

fun <T : Any, K : Any> Server.crud(repository: MongoIdRepository<T, K>, readOnly: Boolean = false) {
    RestCrud (repository, this, readOnly).install()
}

fun <T : Any, K : Any> crud(repository: MongoIdRepository<T, K>, readOnly: Boolean = false) {
    server.crud (repository, readOnly)
}

fun <T : Any> Server.crud(repository: MongoRepository<T>, readOnly: Boolean = false) {
    RestBaseCrud (repository, this, readOnly).install()
}

fun <T : Any> crud(repository: MongoRepository<T>, readOnly: Boolean = false) {
    server.crud (repository, readOnly)
}

// TODO Not optimal at all (can be faster using inputStream instead body string)
fun Server.files(path: String = "/file", fileName: Exchange.() -> String = { request.pathInfo }) {
    get(path) { file(fileName()) }
//    put(path) { FileRepository.store(fileName(), request.body.byteInputStream(), base64 = true)}
    post(path) { FileRepository.store(fileName(), request.body.byteInputStream(), base64 = true) }
//    delete(path) { FileRepository.gridfs.delete(fileName()) }
}
