package co.there4.hexagon.events.rabbitmq

import co.there4.hexagon.helpers.CachedLogger
import co.there4.hexagon.helpers.retry
import co.there4.hexagon.serialization.parse
import co.there4.hexagon.serialization.serialize
import com.rabbitmq.client.AMQP.BasicProperties
import com.rabbitmq.client.Channel
import com.rabbitmq.client.ConnectionFactory
import com.rabbitmq.client.DefaultConsumer
import com.rabbitmq.client.Envelope
import java.nio.charset.Charset
import java.nio.charset.Charset.defaultCharset
import java.util.concurrent.ExecutorService
import kotlin.reflect.KClass

/**
 * TODO .
 * TODO Add content type support
 */
class Handler<T : Any, R : Any>(
    connectionFactory: ConnectionFactory,
    channel: Channel,
    private val executor: ExecutorService,
    private val type: KClass<T>,
    private val handler: (T) -> R) : DefaultConsumer(channel) {

    private companion object : CachedLogger(Handler::class) {
        private const val RETRIES = 5
        private const val DELAY = 50L
    }

    private val client: RabbitClient by lazy { RabbitClient(connectionFactory) }

    /** @see DefaultConsumer.handleDelivery */
    override fun handleDelivery(
        consumerTag: String, envelope: Envelope, properties: BasicProperties, body: ByteArray) {

        executor.execute {
            val charset = properties.contentEncoding ?: defaultCharset().name()
            val correlationId = properties.correlationId
            val replyTo = properties.replyTo

            try {
                trace("Received message ($correlationId) in $charset")
                val request = String(body, Charset.forName(charset))
                trace("Message body:\n$request")
                val input = request.parse(type)
                handleMessage(input, replyTo, correlationId)
            }
            catch (ex: Exception) {
                warn("Error processing message ($correlationId) in $charset", ex)
                handleError(ex, replyTo, correlationId)
            }
            finally {
                retry(RETRIES, DELAY) { channel.basicAck(envelope.deliveryTag, false) }
            }
        }
    }

    private fun handleMessage(message: T, replyTo: String?, correlationId: String?) {
        if (replyTo == null) return

        val response = handler(message)

        val output = when (response) {
            is String -> response
            is Int -> response.toString()
            is Long -> response.toString()
            else -> response.serialize()
        }

        client.publish(replyTo, output, correlationId)
    }

    private fun handleError(exception: Exception, replyTo: String?, correlationId: String?) {
        if (replyTo == null) return

        val message = exception.message ?: ""
        val errorMessage = if (message.isBlank()) exception.javaClass.name else message
        client.publish(replyTo, errorMessage, correlationId)
    }
}
