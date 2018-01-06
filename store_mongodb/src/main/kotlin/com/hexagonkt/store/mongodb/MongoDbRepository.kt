//package com.hexagonkt.store.mongodb
//
//import com.hexagonkt.store.Store
//import com.mongodb.async.client.MongoClients.getDefaultCodecRegistry
//import com.mongodb.client.model.Filters.eq
//import java.util.Objects.requireNonNull
//import java.util.stream.Collectors.toList
//import org.bson.codecs.configuration.CodecRegistries.fromProviders
//import org.bson.codecs.configuration.CodecRegistries.fromRegistries
//import org.slf4j.LoggerFactory.getLogger
//
//import java.util.ArrayList
//import java.util.concurrent.CountDownLatch
//import java.util.concurrent.TimeUnit
//
//import com.mongodb.async.client.FindIterable
//import com.mongodb.async.client.MongoCollection
//import com.mongodb.async.client.MongoDatabase
//import com.mongodb.client.model.*
//import com.sun.xml.internal.ws.spi.db.BindingContextFactory.LOGGER
//import org.bson.Document
//import org.bson.conversions.Bson
//import sun.misc.ObjectInputFilter.Config.createFilter
//import kotlin.reflect.KClass
//import kotlin.reflect.KProperty1
//
///**
// * TODO .
// */
//class MongoDbRepository<T : Any, K : Any> private constructor(
//    val name: String?,
//    override val type: KClass<T>,
//    override val key: KProperty1<T, K>,
//    mongoDatabase: MongoDatabase) : Store<T, K> {
//
//    companion object {
//        private val LOGGER = getLogger(MongoDbRepository::class.java)
//    }
//
//    val name: String
//
//    protected val collection: MongoCollection<Document>
//    private val typedCollection: MongoCollection<T>
//
//    constructor(
//        name: String,
//        type: Class<T>,
//        keyName: String,
//        mongoDatabase: MongoDatabase,
//        generateKey: Boolean) : this(name, Entity(type, keyName, generateKey), mongoDatabase) {
//    }
//
//    init {
//
//        requireNonNull<Any>(entity)
//        requireNonNull(mongoDatabase)
//        this.name = name ?: entity.type.getSimpleName()
//
//        val codecProvider = JacksonCodecProvider(entity)
//        val database = mongoDatabase.withCodecRegistry(
//            fromRegistries(getDefaultCodecRegistry(), fromProviders(codecProvider))
//        )
//
//        val latch = CountDownLatch(1)
//        if (entity.generateKey)
//            database.createCollection(
//                this.name,
//                CreateCollectionOptions().autoIndex(false)
//            ) { result, t -> latch.countDown() }
//        Unchecked.run({ latch.await(5, TimeUnit.SECONDS) })
//
//        collection = database.getCollection(this.name)
//        typedCollection = database.getCollection(this.name, entity.type)
//        typedCollection.createIndex(
//            Indexes.ascending(entity.keyName),
//            IndexOptions().unique(true).background(true)
//        ) { r, t -> LOGGER.info("Index created for: {} with field: {}", this.name, this.entity.keyName) }
//    }
//
//    fun insertOne(`object`: T): Future<K> {
//        requireNonNull(`object`)
//        val future = Future.future()
//        typedCollection.insertOne(`object`) { result, error ->
//            if (error == null)
//                future.complete(getKey(`object`) as K) // TODO Check type! this can fail!
//            else
//                future.fail(error)
//        }
//        return future
//    }
//
//    fun insertMany(objects: List<T>): Future<Void> {
//        requireNonNull(objects)
//        val future = Future.future()
//        if (!objects.isEmpty())
//            typedCollection.insertMany(objects, singleResultCallback(future))
//        return future
//    }
//
//    fun replaceOne(`object`: T): Future<Boolean> {
//        requireNonNull(`object`)
//        val future = Future.future()
//        val filter = eq(entity.keyName, getKey(`object`))
//        typedCollection.replaceOne(filter, `object`) { result, error ->
//            if (error == null)
//                future.complete(result.modifiedCount == 1L)
//            else
//                future.fail(error)
//        }
//        return future
//    }
//
//    fun replaceMany(objects: List<T>): CompositeFuture {
//        requireNonNull(objects)
//        return CompositeFuture.join(
//            objects.stream().map<Any> { `object` ->
//                val future = Future.future()
//                val filter = eq(entity.keyName, getKey(`object`))
//                typedCollection.replaceOne(filter, `object`, singleResultCallback(future))
//                future
//            }
//                .collect<R, A>(toList<T>())
//        )
//    }
//
//    fun updateOne(key: K, updates: Map<String, *>): Future<Boolean> {
//        return updateOne(key, createUpdate(updates))
//    }
//
//    fun updateMany(
//        filter: Map<String, List<*>>,
//        updates: Map<String, *>): Future<Long> {
//
//        return updateMany(createFilter(entity, filter), createUpdate(updates))
//    }
//
//    fun deleteOne(id: K): Future<Boolean> {
//        requireNonNull(id)
//        val future = Future.future()
//        val filter = eq(entity.keyName, convertKey(id))
//        typedCollection.deleteOne(filter) { result, error ->
//            if (error == null)
//                future.complete(result.deletedCount == 1L)
//            else
//                future.fail(error)
//        }
//        return future
//    }
//
//    fun deleteMany(filter: Map<String, List<*>>): Future<Long> {
//        return deleteMany(createFilter(entity, filter))
//    }
//
//    fun findOne(key: K): Future<T> {
//        requireNonNull(key)
//
//        val finalKey = convertKey(key)
//        val future = Future.future()
//        typedCollection.find(eq(entity.keyName, finalKey)).first(singleResultCallback(future))
//        return future
//    }
//
//    fun findOne(key: K, fields: List<String>): Future<Map<String, *>> {
//        return findOne(key, createProjection(entity, fields))
//    }
//
//    fun findMany(
//        pattern: Map<String, List<*>>,
//        limit: Int?,
//        skip: Int?,
//        sort: Map<String, Boolean>): Future<List<T>> {
//
//        return findMany(createFilter(entity, pattern), limit, skip, createSort(entity, sort))
//    }
//
//    fun findMany(
//        pattern: Map<String, List<*>>,
//        projection: List<String>,
//        limit: Int?,
//        skip: Int?,
//        sort: Map<String, Boolean>): Future<List<Map<String, *>>> {
//
//        return findMany(
//            createFilter(entity, pattern),
//            createProjection(entity, projection),
//            limit,
//            skip,
//            createSort(entity, sort))
//    }
//
//    fun count(filter: Map<String, List<*>>): Future<Long> {
//        return count(createFilter(entity, filter))
//    }
//
//    fun updateOne(key: K, update: Bson): Future<Boolean> {
//        requireNonNull(key)
//        requireNonNull(update)
//
//        val future = Future.future()
//        val filter = eq(entity.keyName, convertKey(key))
//        val options = UpdateOptions()
//        typedCollection.updateOne(filter, update, options) { result, error ->
//            if (error == null)
//                future.complete(result.modifiedCount == 1L)
//            else
//                future.fail(error)
//        }
//        return future
//    }
//
//    fun updateMany(filter: Bson, update: Bson): Future<Long> {
//        requireNonNull(filter)
//        requireNonNull(update)
//
//        val future = Future.future()
//        val options = UpdateOptions()
//        typedCollection.updateMany(filter, update, options) { result, error ->
//            if (error == null)
//                future.complete(result.modifiedCount)
//            else
//                future.fail(error)
//        }
//        return future
//    }
//
//    fun deleteMany(pattern: Bson): Future<Long> {
//        requireNonNull(pattern)
//
//        val future = Future.future()
//        typedCollection.deleteMany(pattern) { result, error ->
//            if (error == null)
//                future.complete(result.deletedCount)
//            else
//                future.fail(error)
//        }
//        return future
//    }
//
//    fun findOne(key: K, fields: Bson): Future<Map<String, *>> {
//        val future = Future.future()
//
//        collection
//            .find(eq(entity.keyName, key))
//            .projection(fields)
//            .first { result, error ->
//                if (error == null)
//                    future.complete(result)
//                else
//                    future.fail(error)
//            }
//
//        return future
//    }
//
//    fun findMany(
//        pattern: Bson, projection: Bson, limit: Int?, skip: Int?, sort: Bson): Future<List<Map<String, *>>> {
//
//        val future = Future.future()
//
//        findIterable(pattern, limit, skip, projection, sort)
//            .into(ArrayList()) { result, exception ->
//                if (exception != null) {
//                    future.fail(exception)
//                } else {
//
//                    val mapStream = result.stream()
//                        .map { it -> it as Map<String, *> }
//                        .collect<List<Map<String, *>>, Any>(toList())
//
//                    future.complete(mapStream)
//                }
//            }
//
//        return future
//    }
//
//    private fun findIterable(
//        pattern: Bson?, limit: Int?, skip: Int?, projection: Bson?, sort: Bson?): FindIterable<Document> {
//
//        val iterable = if (pattern == null)
//            collection.find()
//        else
//            collection.find(pattern)
//
//        if (limit != null)
//            iterable.limit(limit)
//        if (skip != null)
//            iterable.skip(skip)
//
//        if (projection != null)
//            iterable.projection(projection)
//
//        if (sort != null)
//            iterable.sort(sort)
//
//        return iterable
//    }
//
//    private fun findIterable(pattern: Bson?, limit: Int?, skip: Int?, sort: Bson?): FindIterable<T> {
//
//        val iterable = if (pattern == null)
//            typedCollection.find()
//        else
//            typedCollection.find(pattern)
//
//        if (limit != null)
//            iterable.limit(limit)
//        if (skip != null)
//            iterable.skip(skip)
//
//        if (sort != null)
//            iterable.sort(sort)
//
//        return iterable
//    }
//
//    fun findMany(pattern: Bson, limit: Int?, skip: Int?, sort: Bson): Future<List<T>> {
//        val future = Future.future()
//        val callback = singleResultCallback(future)
//        findIterable(pattern, limit, skip, sort).into<List<T>>(ArrayList(), callback)
//        return future
//    }
//
//    fun count(pattern: Bson): Future<Long> {
//        val future = Future.future()
//        typedCollection.count(pattern, singleResultCallback(future))
//        return future
//    }
//
//    private fun convertKey(`object`: K): Any {
//        return if (entity.generateKey) oid(`object`.toString()) else `object`
//    }
//
//    private fun getKey(`object`: T): Any {
//        val key = entity.keySupplier.apply(`object`)
//        return if (entity.generateKey) oid(key.toString()) else key
//    }
//}
