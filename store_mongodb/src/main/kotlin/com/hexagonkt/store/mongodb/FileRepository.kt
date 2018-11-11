package com.hexagonkt.store.mongodb

import com.hexagonkt.helpers.error
import com.mongodb.client.gridfs.GridFSBucket
import com.mongodb.client.gridfs.GridFSBuckets
import com.mongodb.client.gridfs.model.GridFSUploadOptions
import org.bson.Document
import org.bson.types.ObjectId
import java.io.InputStream
import java.io.OutputStream
import java.util.Base64.getDecoder as base64Decoder

/**
 * .
 */
object FileRepository {
    /** . */
    private val gridfs: GridFSBucket = GridFSBuckets.create(mongoDatabase())

    private val decoder = base64Decoder()

    private fun meta(metadata: Map<String, *>) = GridFSUploadOptions().metadata(Document(metadata))

    /**
     * .
     *
     * @param name .
     * @param input .
     * @param metadata .
     * @return .
     */
    fun store(
        name: String,
        input: InputStream,
        metadata: Map<String, *> = mapOf<String, Any>(),
        base64: Boolean = false): ObjectId? =
            gridfs.uploadFromStream(name, if(base64) decoder.wrap(input) else input, meta(metadata))

    /**
     * .
     *
     * @param name .
     * @param output .
     * @return .
     */
    fun load(name: String, output: OutputStream): Map<String, *> {
        gridfs.downloadToStream(name, output)
        return gridfs.find("filename" eq name).first()?.metadata ?: error
    }
}
