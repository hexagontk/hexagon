package co.there4.hexagon.rest

import java.net.InetAddress.getByName as address

// TODO Not optimal at all (can be faster using inputStream instead body string)
//fun Server.files(path: String = "/file", fileName: Exchange.() -> String = { request.pathInfo }) {
//    get(path) { file(fileName()) }
//    put(path) { FileRepository.store(fileName(), request.body.byteInputStream(), base64 = true)}
//    post(path) { FileRepository.store(fileName(), request.body.byteInputStream(), base64 = true) }
//    delete(path) { FileRepository.gridfs.delete(fileName()) }
//}
