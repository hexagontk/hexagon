package co.there4.hexagon.messaging

import co.there4.hexagon.serialization.parse
import co.there4.hexagon.util.*
import com.rabbitmq.client.AMQP
import com.rabbitmq.client.Channel
import com.rabbitmq.client.DefaultConsumer
import com.rabbitmq.client.Envelope
import java.nio.charset.Charset
import java.util.concurrent.ExecutorService
import kotlin.reflect.KClass

abstract class Handler<T : Any> (
    channel: Channel, val executor: ExecutorService, val type: KClass<T>):
    DefaultConsumer (channel) {

    companion object : CompanionLogger (Handler::class) {
        val RETRIES = 5
        val DELAY = 50L
    }

    override fun handleDelivery(
        consumerTag: String?,
        envelope: Envelope?,
        properties: AMQP.BasicProperties?,
        body: ByteArray?) {

        executor.execute {
            pushTime ()

            val encoding = Charset.forName(properties?.contentEncoding) ?: Charset.defaultCharset()
            val correlationId = properties?.correlationId
            val replyTo = properties?.replyTo

            var request: String? = null

            try {
                if (body == null)
                    throw IllegalStateException ("'Null' body")

                request = String(body, encoding)
                val input = request.parse(type)
                handleMessage(input, replyTo, correlationId)
            }
            catch (ex: Exception) {
                warn("Error processing message", ex)
                handleError(ex, replyTo, correlationId)
            }
            finally {
                val deliveryTag =
                    envelope?.deliveryTag ?: throw IllegalStateException ("No delivery tag")
                retry (RETRIES, DELAY) { channel.basicAck (deliveryTag, false) }
                trace (
                    """
                    ENCODING: ${encoding.name()} CORRELATION ID: $correlationId
                    TIME: ${formatTime(popTime())}
                    BODY: $request"""
                )
            }
        }
    }

    protected abstract fun handleMessage(message: T, replyTo: String?, correlationId: String?)

    protected open fun handleError(exception: Exception, replyTo: String?, correlationId: String?) {
        // Empty error handler
    }
}
