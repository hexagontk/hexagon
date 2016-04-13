package co.there4.hexagon.messaging

import co.there4.hexagon.util.CompanionLogger
import co.there4.hexagon.util.retry
import com.rabbitmq.client.*
import net.jodah.lyra.Connections
import net.jodah.lyra.config.Config
import net.jodah.lyra.config.RecoveryPolicy
import net.jodah.lyra.util.Duration
import java.io.Closeable
import java.lang.Runtime.getRuntime
import java.net.URI
import java.nio.charset.Charset.defaultCharset
import java.util.*
import java.util.UUID.randomUUID
import java.util.concurrent.Executors.newFixedThreadPool
import kotlin.reflect.KClass

/**
 * Rabbit client.
 * TODO Add metrics
 * TODO Ordered shutdown
 *
 * @author jam
 */
class RabbitClient (
    val connectionFactory: ConnectionFactory,
    val poolSize: Int = getRuntime().availableProcessors()) : Closeable {

    companion object : CompanionLogger (RabbitClient::class) {
        internal fun parseQueryParameters(query: String): Map<String, String> =
            if (query.isEmpty())
                mapOf()
            else
                query.split("&".toRegex())
                    .map { it.split("=") }
                    .map { it[0].trim () to it[1].trim() }
                    .toMap(LinkedHashMap<String, String>())

        fun createConnectionFactory(uri: String): ConnectionFactory {
            fun <T> setVar(value: T?, setter: (T) -> Unit) {
                if (value != null)
                    setter (value)
            }

            require(!uri.isEmpty())

            val rabbitUri = URI(uri)
            val cf = ConnectionFactory()
            cf.setUri(rabbitUri)
            cf.exceptionHandler = LogExceptionHandler()

            val params = parseQueryParameters(rabbitUri.query ?: "")
            setVar(params["automaticRecovery"]?.toBoolean()) { cf.isAutomaticRecoveryEnabled = it }
            setVar(params["recoveryInterval"]?.toLong()) { cf.networkRecoveryInterval = it }
            setVar(params["shutdownTimeout"]?.toInt()) { cf.shutdownTimeout = it }
            setVar(params["topologyRecovery"]?.toBoolean()) { cf.isTopologyRecoveryEnabled = it }
            setVar(params["heartbeat"]?.toInt()) { cf.requestedHeartbeat = it }

            return cf
        }
    }

    @Volatile private var count: Int = 0
    private val threadPool = newFixedThreadPool(poolSize, { Thread(it, "rabbitmq-" + count++) })
    private var connection: Connection? = null

    init {
        val config = Config()
            .withRecoveryPolicy(RecoveryPolicy()
            .withBackoff(Duration.seconds(1), Duration.seconds(30))
            .withMaxAttempts(20))

        this.connection = Connections.create(connectionFactory, config)

        val topologyRecoveryEnabled =
            if (connectionFactory.isTopologyRecoveryEnabled) "TOPOLOGY RECOVERY"
            else "NO TOPOLOGY RECOVERY"

        info("""
            RabbitMQ Client connected to:
            ${connectionFactory.host}
            ${connectionFactory.port}
            ${connectionFactory.virtualHost}
            ${connectionFactory.username}
            ${connectionFactory.password}
            ${topologyRecoveryEnabled}""".trimIndent()
        )
    }

    constructor (uri: String): this(createConnectionFactory(uri))

    override fun close() {
        if (connection == null || connection?.isOpen() ?: false)
            throw IllegalStateException("Connection already closed")

        connection?.close()
        info("RabbitMQ client closed")
    }

    fun declareQueue(name: String) {
        withChannel { it.queueDeclare(name, false, false, false, null) }
    }

    fun deleteQueue(name: String) {
        withChannel { it.queueDelete(name) }
    }

    fun <T : Any> consume(
        exchange: String, routingKey: String, type: KClass<T>, handler: (T) -> Unit) {

        withChannel {
            it.queueDeclare(routingKey, false, false, false, null)
            it.queueBind(routingKey, exchange, routingKey)
        }
        consume(routingKey, type, handler)
    }

    fun <T : Any> consume(queueName: String, type: KClass<T>, handler: (T) -> Unit) {
        val channel = createChannel()
        val callback = Consumer(channel, threadPool, type, handler)
        channel.basicConsume(queueName, false, callback)
        info("Consuming messages in $queueName")
    }

    fun <T : Any, R : Any> reply(queueName: String, type: KClass<T>, handler: (T) -> R) {
        val channel = createChannel()
        val callback = Replier(connectionFactory, channel, threadPool, type, handler)
        channel.basicConsume(queueName, false, callback)
        info("Consuming messages in $queueName")
    }

    /**
     * Tries to get a channel for five times. If it do not succeed it throws an
     * IllegalStateException.
     *
     * @return A new channel.
     */
    private fun createChannel(): Channel =
        retry (times = 3, delay = 50) {
            if (!(connection?.isOpen ?: false)) {
                connection = connectionFactory.newConnection()
                warn("Rabbit connection RESTORED")
            }
            val channel = connection!!.createChannel()
            channel.basicQos(poolSize)
            channel
        }

    private fun <T> withChannel(callback: (Channel) -> T): T {
        var channel: Channel? = null
        try {
            channel = createChannel()
            return callback(channel)
        }
        finally {
            try {
                if (channel != null && channel.isOpen)
                    channel.close()
            }
            catch (e: AlreadyClosedException) {
                // Lyra throws this calling 'isOpen' on a closed channel (this is a bug in Lyra)
            }
        }
    }

    fun publish(
        exchange: String, routingKey: String, message: String, correlationId: String? = null) {

        withChannel { channel ->
            val charset = defaultCharset().name()
            publish(channel, exchange, routingKey, charset, message, correlationId, null)
        }
    }

    internal fun publish(
        channel: Channel,
        exchange: String,
        routingKey: String,
        encoding: String,
        message: String,
        correlationId: String?,
        replyQueueName: String?) {

        val builder = AMQP.BasicProperties.Builder()

        if (correlationId != null && !correlationId.isEmpty())
            builder.correlationId(correlationId)

        if (replyQueueName != null && !replyQueueName.isEmpty())
            builder.replyTo(replyQueueName)

        builder.contentEncoding(encoding)

        val props = builder.build()

        channel.basicPublish(exchange, routingKey, props, message.toByteArray(charset(encoding)))

        debug(
            """
            EXCHANGE: $exchange ROUTING KEY: $routingKey
            REPLY TO: $replyQueueName CORRELATION ID: $correlationId
            BODY:
            $message""".trimIndent())
    }

    fun call(requestQueue: String, message: String): String =
        withChannel {
            val correlationId = randomUUID().toString()
            val replyQueueName = it.queueDeclare().getQueue()
            val charset = defaultCharset().name()

            publish(it, "", requestQueue, charset, message, correlationId, replyQueueName)

            val consumer = QueueingConsumer(it)
            it.basicConsume(replyQueueName, true, consumer)

            var result: String? = null
            while (result == null) {
                try {
                    val delivery = consumer.nextDelivery(20000)
                    if (delivery != null && delivery.properties.correlationId == correlationId)
                        result = String(delivery.body)
                }
                catch (e: Exception) {
                    throw RuntimeException("Timeout listening queue", e)
                }
            }
            result
        }
}
