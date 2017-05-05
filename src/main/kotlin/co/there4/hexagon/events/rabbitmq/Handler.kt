package co.there4.hexagon.events.rabbitmq

import co.there4.hexagon.serialization.parse
import co.there4.hexagon.serialization.serialize
import co.there4.hexagon.helpers.*
import com.rabbitmq.client.*
import java.nio.charset.Charset
import java.nio.charset.Charset.defaultCharset
import java.util.concurrent.ExecutorService
import kotlin.reflect.KClass
import kotlin.system.measureNanoTime

class Handler<T : Any, R : Any> (
    connectionFactory: ConnectionFactory,
    channel: Channel,
    private val executor: ExecutorService,
    val type: KClass<T>,
    private val handler: (T) -> R): DefaultConsumer(channel) {

    companion object : CachedLogger(Handler::class) {
        const val RETRIES = 5
        const val DELAY = 50L
    }

    private val client: RabbitClient = RabbitClient(connectionFactory)

    override fun handleDelivery(
        consumerTag: String,
        envelope: Envelope,
        properties: AMQP.BasicProperties,
        body: ByteArray) {

        executor.execute {
            val t = measureNanoTime {
                val charset = properties.contentEncoding ?: defaultCharset().name()
                val correlationId = properties.correlationId
                val replyTo = properties.replyTo

                var request: String? = null

                try {
                    request = String(body, Charset.forName(charset))
                    val input = request.parse(type)
                    handleMessage(input, replyTo, correlationId)
                }
                catch (ex: Exception) {
                    warn("Error processing message", ex)
                    handleError(ex, replyTo, correlationId)
                }
                finally {
                    retry (RETRIES, DELAY) { channel.basicAck (envelope.deliveryTag, false) }
                    trace (
                        """ENCODING: $charset CORRELATION ID: $correlationId
                        BODY: $request"""
                    )
                }
            }
            trace ("TIME: ${formatNanos(t)}")
        }
    }

    private fun handleMessage(message: T, replyTo: String?, correlationId: String?) {
        val response = handler(message)

        val output = when (response) {
            is String -> response
            is Int -> response.toString()
            is Long -> response.toString()
            else -> response.serialize()
        }

        if (replyTo != null)
            client.publish("", replyTo, output, correlationId)
    }

    private fun handleError(exception: Exception, replyTo: String?, correlationId: String?) {
        if (replyTo != null) {
            val message = exception.message ?: ""
            val errorMessage = if (message.isBlank()) exception.javaClass.name else message
            client.publish("", replyTo, errorMessage, correlationId)
        }
    }
}
