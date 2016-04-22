package co.there4.hexagon.repository

import co.there4.hexagon.serialization.convertToMap
import co.there4.hexagon.serialization.convertToObject
import com.mongodb.client.MongoCollection
import com.mongodb.client.MongoIterable
import com.mongodb.client.model.FindOneAndReplaceOptions
import com.mongodb.client.model.InsertManyOptions
import com.mongodb.client.model.InsertOneOptions
import com.mongodb.client.model.UpdateOptions
import com.mongodb.client.result.DeleteResult
import com.mongodb.client.result.UpdateResult
import org.bson.Document
import org.bson.conversions.Bson
import kotlin.reflect.KClass

open class MongoRepository <T : Any> (val type: KClass<T>, collection: MongoCollection<Document>) :
    MongoCollection<Document> by collection {

    fun insertOneObject (document: T) {
        insertOne (map (document))
    }

    fun insertOneObject (document: T, options: InsertOneOptions) {
        insertOne (map (document), options)
    }

    fun insertManyObjects (documents: List<T>) {
        insertMany (map (documents))
    }

    fun insertManyObjects (documents: List<T>, options: InsertManyOptions) {
        insertMany (map (documents), options)
    }

    fun replaceOneObject (filter: Bson, replacement: T): UpdateResult =
        replaceOne (filter, map (replacement))

    fun replaceOneObject (filter: Bson, replacement: T, options: UpdateOptions): UpdateResult =
        replaceOne (filter, map (replacement), options)

    fun findOneObjectAndReplace (filter: Bson, replacement: T): T =
        unmap (findOneAndReplace (filter, map (replacement)))

    fun findOneObjectAndReplace (filter: Bson, replacement: T, options: FindOneAndReplaceOptions): T =
        unmap (findOneAndReplace (filter, map (replacement), options))

    fun findObjects (): MongoIterable<T> = find ().map { unmap(it) }

    fun findObjects (filter: Bson): MongoIterable<T> = find (filter).map { unmap(it) }

    fun deleteAll (): DeleteResult = deleteMany (Document ())

    private fun map (document: T): Document {
        return Document (document.convertToMap ().mapKeys {
            val key = it.key ?: throw IllegalStateException ("Key can not be 'null'")
            if (key is String)
                key
            else
                throw IllegalStateException ("Key must be 'String' not '${key.javaClass.name}'")
        })
    }

    private fun map (documents: List<T>): List<Document> = documents.map { map(it) }

    private fun unmap (document: Document): T = document.convertToObject (type)
}
