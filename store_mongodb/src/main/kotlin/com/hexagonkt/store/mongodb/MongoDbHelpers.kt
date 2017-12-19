package com.hexagonkt.store.mongodb

import com.mongodb.client.model.Sorts
import com.mongodb.client.model.Sorts.orderBy
import com.mongodb.client.model.Updates
import org.bson.conversions.Bson

/**
 * TODO .
 */
class MongoDbHelpers {
    fun createUpdate(update: Map<String, *>): Bson =
        Updates.combine (update.entries.map { Updates.set (it.key, it.value) }.toList ())

//    fun createSort(fields: Map<String, Boolean>): Bson =
//        orderBy (
//            fields.entries
//                .filter {it -> entity.fields.contains (it.key)}
//                .map {it -> if (it.value) descending (it.key) else Sorts.ascending (it.key) }
//                .toList()
//        )
}
