package com.hexagonkt.rest

import java.net.InetAddress.getByName as address

// TODO Not optimal at all (can be faster using inputStream instead body string)
//fun Server.files(path: String = "/file", fileName: Call.() -> String = { request.pathInfo }) {
//    get(path) { file(fileName()) }
//    put(path) { FileRepository.store(fileName(), request.body.byteInputStream(), base64 = true)}
//    post(path) { FileRepository.store(fileName(), request.body.byteInputStream(), base64 = true) }
//    delete(path) { FileRepository.gridfs.delete(fileName()) }
//}

//fun Call.file(name: String) {
//    val meta = load(name, response.outputStream)
//    response.contentType = meta["Content-Type"].toString()
//    response.outputStream.flush()
//    response.status = 200
//}
