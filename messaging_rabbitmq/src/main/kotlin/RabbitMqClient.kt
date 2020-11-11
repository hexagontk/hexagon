package com.hexagonkt.messaging.rabbitmq

import com.hexagonkt.logging.Logger
import com.codahale.metrics.MetricRegistry
import com.hexagonkt.http.parseQueryParameters
import com.hexagonkt.helpers.*
import com.rabbitmq.client.*
import com.rabbitmq.client.AMQP.BasicProperties
import com.rabbitmq.client.impl.StandardMetricsCollector

import java.io.Closeable
import java.lang.Runtime.getRuntime
import java.net.URI
import java.nio.charset.Charset.defaultCharset
import java.util.UUID.randomUUID
import java.util.concurrent.ArrayBlockingQueue
import java.util.concurrent.Executors.newFixedThreadPool
import kotlin.reflect.KClass

/**
 * Rabbit client.
 *
 * * TODO Review if channel handling is still needed in Java 4.1.x version
 * * TODO Add metrics
 * * TODO Ordered shutdown
 */
class RabbitMqClient(
    private val connectionFactory: ConnectionFactory,
    private val poolSize: Int = getRuntime().availableProcessors()) : Closeable {

    internal companion object {

        private fun <T> setVar(value: T?, setter: (T) -> Unit) {
            if (value != null)
                setter(value)
        }

        internal fun createConnectionFactory(uri: URI): ConnectionFactory {
            require(uri.toString().isNotBlank())

            val cf = ConnectionFactory()
            cf.setUri(uri)

            val params = parseQueryParameters(uri.query ?: "")
            fun value(name: String): String? = params[name]?.firstOrNull { it.isNotBlank() }
            val automaticRecovery = value("automaticRecovery")?.toBoolean()
            val recoveryInterval = value("recoveryInterval")?.toLong()
            val shutdownTimeout = value("shutdownTimeout")?.toInt()
            val heartbeat = value("heartbeat")?.toInt()
            val metricsCollector = StandardMetricsCollector(MetricRegistry())

            setVar(automaticRecovery) { cf.isAutomaticRecoveryEnabled = it }
            setVar(recoveryInterval) { cf.networkRecoveryInterval = it }
            setVar(shutdownTimeout) { cf.shutdownTimeout = it }
            setVar(heartbeat) { cf.requestedHeartbeat = it }
            setVar(metricsCollector) { cf.metricsCollector = it }

            return cf
        }
    }

    private val log: Logger = Logger(this::class)
    private val args = hashMapOf<String, Any>()

    @Volatile private var count: Int = 0
    private val threadPool = newFixedThreadPool(poolSize) { Thread(it, "rabbitmq-" + count++) }
    private var connection: Connection? = connectionFactory.newConnection()
    private val metrics: Metrics = Metrics(connectionFactory.metricsCollector as StandardMetricsCollector)
    private val listener = ConnectionListener()

    /** . */
    constructor (uri: URI) : this(createConnectionFactory(uri))

    /** . */
    val connected: Boolean get() = connection?.isOpen ?: false

    /** @see Closeable.close */
    override fun close() {
        connection?.removeShutdownListener(listener)
        (connection as? Recoverable)?.removeRecoveryListener(listener)
        connection?.close()
        connection = null
        metrics.report()
        log.info { "RabbitMQ client closed" }
    }

    /** . */
    fun declareQueue(name: String) {
        args["x-max-length-bytes"] = 1048576  // max queue length
        withChannel { it.queueDeclare(name, false, false, false, args) }
    }

    /** . */
    fun deleteQueue(name: String) {
        withChannel { it.queueDelete(name) }
    }

    /** . */
    fun bindExchange(exchange: String, exchangeType: String, routingKey: String, queue: String) {
        withChannel {
            it.queueDeclare(queue, false, false, false, null)
            it.queuePurge(queue)
            it.exchangeDeclare(exchange, exchangeType, false, false, false, null)
            it.queueBind(queue, exchange, routingKey)
        }
    }

    /** . */
    fun <T : Any> consume(
        exchange: String, routingKey: String, type: KClass<T>, handler: (T) -> Unit) {

        withChannel {
            it.queueDeclare(routingKey, false, false, false, null)
            it.queuePurge(routingKey)
            it.queueBind(routingKey, exchange, routingKey)
        }
        consume(routingKey, type, handler)
    }

    fun <T : Any, R : Any> consume(queueName: String, type: KClass<T>, handler: (T) -> R) {
        val channel = createChannel()
        val callback = Handler(connectionFactory, channel, threadPool, type, handler)
        channel.basicConsume(queueName, false, callback)
        log.info { "Consuming messages in $queueName" }
    }

    /**
     * Tries to get a channel for five times. If it do not succeed it throws an
     * IllegalStateException.
     *
     * @return A new channel.
     */
    private fun createChannel(): Channel =
        retry(times = 3, delay = 50) {
            if (connection?.isOpen != true) {
                connection = connectionFactory.newConnection()
                connection?.addShutdownListener(listener)
                (connection as Recoverable).addRecoveryListener(listener)
                log.warn { "Rabbit connection RESTORED" }
            }
            val channel = connection?.createChannel() ?: fail
            channel.basicQos(poolSize)
            channel.addShutdownListener(listener)
            (channel as Recoverable).addRecoveryListener(listener)
            channel
        }

    private fun <T> withChannel(callback: (Channel) -> T): T {
        var channel: Channel? = null
        try {
            channel = createChannel()
            return callback(channel)
        }
        finally {
            if (channel != null && channel.isOpen)
                channel.close()
        }
    }

    fun publish(queue: String, message: String, correlationId: String? = null) =
        publish("", queue, message, correlationId)

    fun publish(
        exchange: String,
        routingKey: String,
        message: String,
        correlationId: String? = null) {

        withChannel { channel ->
            publish(channel, exchange, routingKey, null, message, correlationId, null)
        }
    }

    private fun publish(
        channel: Channel,
        exchange: String,
        routingKey: String,
        encoding: String?,
        message: String,
        correlationId: String?,
        replyQueueName: String?) {

        val builder = BasicProperties.Builder()

        if (!correlationId.isNullOrBlank())
            builder.correlationId(correlationId)

        if (!replyQueueName.isNullOrBlank())
            builder.replyTo(replyQueueName)

        if (!encoding.isNullOrBlank())
            builder.contentEncoding(encoding)

        val props = builder.build()

        val charset = if (encoding == null) defaultCharset() else charset(encoding)
        channel.basicPublish(exchange, routingKey, props, message.toByteArray(charset))

        log.debug {
            """
            EXCHANGE: $exchange ROUTING KEY: $routingKey
            REPLY TO: $replyQueueName CORRELATION ID: $correlationId
            BODY:
            $message""".trimIndent()
        }
    }

    fun call(requestQueue: String, message: String): String =
        withChannel {
            val correlationId = randomUUID().toString()
            val replyQueueName = it.queueDeclare().queue
            val charset = defaultCharset().name()

            publish(it, "", requestQueue, charset, message, correlationId, replyQueueName)

            val response = ArrayBlockingQueue<String>(1)
            val consumer = object : DefaultConsumer(it) {
                override fun handleDelivery(
                    consumerTag: String?,
                    envelope: Envelope?,
                    properties: BasicProperties?,
                    body: ByteArray?) {

                    if (properties?.correlationId == correlationId)
                        response.offer(String(body ?: byteArrayOf()))
                }

                override fun handleCancelOk(consumerTag: String) {
                    log.debug { "Explicit cancel for the consumer $consumerTag" }
                }
            }

            val ctag = it.basicConsume(replyQueueName, true, consumer)

            val result: String = response.take() // Wait until there is an element in the array blocking queue
            it.basicCancel(ctag)
            result
        }
}
