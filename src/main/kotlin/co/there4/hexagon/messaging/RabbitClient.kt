package co.there4.hexagon.messaging

import co.there4.hexagon.settings.SettingsManager.setting
import co.there4.hexagon.util.CompanionLogger
import co.there4.hexagon.util.parseQueryParameters
import co.there4.hexagon.util.retry
import com.rabbitmq.client.*
import com.rabbitmq.client.AMQP.BasicProperties
import java.io.Closeable
import java.lang.Runtime.getRuntime
import java.lang.Thread.sleep
import java.net.URI
import java.nio.charset.Charset.defaultCharset
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
    private val connectionFactory: ConnectionFactory,
    private val poolSize: Int = getRuntime().availableProcessors()) : Closeable {

    companion object : CompanionLogger (RabbitClient::class) {
        val rabbitmqUrl = setting<String>("rabbitmqUrl") ?: "amqp://guest:guest@localhost"

        fun createConnectionFactory(uri: String = rabbitmqUrl): ConnectionFactory {
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
            setVar(params["heartbeat"]?.toInt()) { cf.requestedHeartbeat = it }

            return cf
        }
    }

    @Volatile private var count: Int = 0
    private val threadPool = newFixedThreadPool(poolSize, { Thread(it, "rabbitmq-" + count++) })
    private var connection: Connection? = null

    init {
        this.connection = connectionFactory.newConnection()

        info("""
            RabbitMQ Client connected to:
            ${connectionFactory.host}
            ${connectionFactory.port}
            ${connectionFactory.virtualHost}
            ${connectionFactory.username}
            ${connectionFactory.password}""".trimIndent()
        )
    }

    constructor (uri: String = rabbitmqUrl): this(createConnectionFactory(uri))

    val connected: Boolean get() = connection?.isOpen ?: false

    override fun close() {
        if (!connected) error("Connection already closed")

        connection?.close()
        info("RabbitMQ client closed")
    }

    fun declareQueue(name: String) {
        withChannel { it.queueDeclare(name, false, false, false, null) }
    }

    fun deleteQueue(name: String) {
        withChannel { it.queueDelete(name) }
    }

    fun bindExchange (exchange: String, exchangeType: String, routingKey: String, queue: String) {
        withChannel {
            it.queueDeclare(queue, false, false, false, null)
            it.exchangeDeclare(exchange, exchangeType, false, false, false, null)
            it.queueBind(queue, exchange, routingKey)
        }
    }

    fun <T : Any> consume(
        exchange: String, routingKey: String, type: KClass<T>, handler: (T) -> Unit) {

        withChannel {
            it.queueDeclare(routingKey, false, false, false, null)
            it.queueBind(routingKey, exchange, routingKey)
        }
        consume(routingKey, type, handler)
    }

    fun <T : Any, R : Any> consume(queueName: String, type: KClass<T>, handler: (T) -> R) {
        val channel = createChannel()
        val callback = Handler(connectionFactory, channel, threadPool, type, handler)
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
            if (channel != null && channel.isOpen)
                channel.close()
        }
    }

    fun publish(queue: String, message: String) = publish("", queue, message)

    fun publish(
        exchange: String,
        routingKey: String,
        message: String,
        correlationId: String? = null) {

        withChannel { channel ->
            publish(channel, exchange, routingKey, null, message, correlationId, null)
        }
    }

    internal fun publish(
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
            val replyQueueName = it.queueDeclare().queue
            val charset = defaultCharset().name()

            publish(it, "", requestQueue, charset, message, correlationId, replyQueueName)

            var result: String? = null
            val consumer = object : DefaultConsumer(it) {
                override fun handleDelivery(
                    consumerTag: String?,
                    envelope: Envelope?,
                    properties: BasicProperties?,
                    body: ByteArray?) {

                    if (properties?.correlationId == correlationId)
                        result = String(body ?: byteArrayOf())
                }
            }

            it.basicConsume(replyQueueName, true, consumer)
            while(result == null) { sleep(5) } // Wait until callback is called
            result ?: ""
        }
}
