package co.there4.hexagon.messaging

import co.there4.hexagon.serialization.parse
import co.there4.hexagon.serialization.serialize
import co.there4.hexagon.util.*
import com.rabbitmq.client.*
import java.nio.charset.Charset
import java.util.concurrent.ExecutorService
import kotlin.reflect.KClass

class Handler<T : Any, R : Any> (
    connectionFactory: ConnectionFactory,
    channel: Channel,
    private val executor: ExecutorService,
    val type: KClass<T>,
    private val handler: (T) -> R):
    DefaultConsumer (channel) {

    companion object : CompanionLogger (Handler::class) {
        val RETRIES = 5
        val DELAY = 50L
    }

    private val client: RabbitClient = RabbitClient (connectionFactory)

    override fun handleDelivery(
        consumerTag: String,
        envelope: Envelope,
        properties: AMQP.BasicProperties,
        body: ByteArray) {

        executor.execute {
            pushTime ()

            val encoding = Charset.forName(properties.contentEncoding) ?: Charset.defaultCharset()
            val correlationId = properties.correlationId
            val replyTo = properties.replyTo

            var request: String? = null

            try {
                request = String(body, encoding)
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
                    """
                    ENCODING: ${encoding.name()} CORRELATION ID: $correlationId
                    TIME: ${formatTime(popTime())}
                    BODY: $request"""
                )
            }
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

        if (replyTo != null && correlationId != null)
            client.publish("", replyTo, output, correlationId)
    }

    private fun handleError(exception: Exception, replyTo: String?, correlationId: String?) {
        if (replyTo != null && correlationId != null)
            client.publish("", replyTo, exception.message ?: exception.javaClass.name, correlationId)
    }
}
