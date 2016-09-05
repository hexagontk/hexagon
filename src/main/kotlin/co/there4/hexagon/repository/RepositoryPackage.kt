package co.there4.hexagon.repository

import co.there4.hexagon.settings.SettingsManager
import co.there4.hexagon.events.EventManager
import com.mongodb.MongoClient
import com.mongodb.MongoClientURI
import com.mongodb.client.MongoCollection
import com.mongodb.client.MongoDatabase
import org.bson.Document
import org.bson.conversions.Bson
import org.bson.types.ObjectId
import kotlin.reflect.KClass
import kotlin.reflect.KProperty1
import com.mongodb.client.model.Filters.eq as mEq
import com.mongodb.client.model.Filters.or as mOr
import com.mongodb.client.model.Filters.and as mAnd
import com.mongodb.client.model.Filters.`in` as mIn

val mongodbUrl = SettingsManager["mongodbUrl"] as String? ?: "mongodb://localhost/test"

fun mongoDatabase (uri: String = mongodbUrl): MongoDatabase =
    MongoClient(MongoClientURI(uri)).getDatabase(MongoClientURI(uri).database) ?:
        error ("Error connecting to MongoDB at: $uri")

fun mongoCollection (
    name: String, database: MongoDatabase = mongoDatabase()) : MongoCollection<Document> =
        database.getCollection(name) ?: error ("Error getting '$name' collection")

fun mongoId(): String = ObjectId().toHexString()

inline fun <reified T : Any> mongoRepository(
    database: MongoDatabase = mongoDatabase(), publishEvents: Boolean = false) =
        MongoRepository(T::class, mongoCollection(T::class.simpleName ?: error(""), database), publishEvents )

inline fun <reified T : Any, reified K : Any> mongoIdRepository(
    database: MongoDatabase,
    key: KProperty1<T, K>,
    publishEvents: Boolean = false,
    indexOrder: Int = 1,
    createIndex: Boolean = true) =
    MongoIdRepository (
        T::class,
        mongoCollection(T::class.simpleName ?: error("Error getting type name"), database),
        key,
        publishEvents,
        indexOrder,
        createIndex
    )

inline fun <reified T : Any, reified K : Any> mongoIdRepository(
    key: KProperty1<T, K>,
    publishEvents: Boolean = false,
    indexOrder: Int = 1,
    createIndex: Boolean = true) =
        mongoIdRepository ( mongoDatabase(), key, publishEvents, indexOrder, createIndex)

infix fun Bson.or(value: Bson): Bson = mOr(this, value)
infix fun Bson.and(value: Bson): Bson = mAnd(this, value)
infix fun <T> String.eq(value: T): Bson = mEq(this, value)
infix fun <T> String.isIn(value: List<T>): Bson = mIn(this, value)

fun <T : Any> on (
    entity: KClass<T>, action: RepositoryEventAction, callback: (RepositoryEvent<T>) -> Unit) {
    @Suppress("UNCHECKED_CAST")
    EventManager.on(
        entity.simpleName + "." + action.toString(),
        callback as (RepositoryEvent<*>) -> Unit
    )
}
