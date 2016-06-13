package co.there4.hexagon.repository

import co.there4.hexagon.configuration.ConfigManager
import co.there4.hexagon.events.EventManager
import com.mongodb.MongoClient
import com.mongodb.MongoClientURI
import com.mongodb.client.MongoCollection
import com.mongodb.client.MongoDatabase
import org.bson.Document
import kotlin.reflect.KClass

val mongodbUrl = ConfigManager["mongodbUrl"] as String? ?: "mongodb://localhost/test"

fun mongoDatabase (uri: String = mongodbUrl): MongoDatabase =
    MongoClient(MongoClientURI(uri)).getDatabase(MongoClientURI(uri).database) ?:
        error ("Error connecting to MongoDB at: $uri")

fun mongoCollection (
    name: String, database: MongoDatabase = mongoDatabase()) : MongoCollection<Document> =
        database.getCollection(name) ?: error ("Error getting '$name' collection")

fun <T : Any> on (
    entity: KClass<T>, action: RepositoryEventAction, callback: (RepositoryEvent<T>) -> Unit) {
    @Suppress("UNCHECKED_CAST")
    EventManager.on (
        RepositoryEvent::class,
        entity.simpleName + "." + action.toString(),
        callback as (RepositoryEvent<*>) -> Unit
    )
}
