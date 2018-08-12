package com.hexagonkt.messaging.rabbitmq

import com.hexagonkt.helpers.Loggable
import com.hexagonkt.helpers.loggerOf
import com.hexagonkt.helpers.retry
import com.hexagonkt.serialization.SerializationManager.formatsMap
import com.hexagonkt.serialization.SerializationManager.defaultFormat
import com.hexagonkt.serialization.parse
import com.hexagonkt.serialization.serialize
import com.rabbitmq.client.AMQP.BasicProperties
import com.rabbitmq.client.Channel
import com.rabbitmq.client.ConnectionFactory
import com.rabbitmq.client.DefaultConsumer
import com.rabbitmq.client.Envelope
import org.slf4j.Logger
import java.nio.charset.Charset
import java.nio.charset.Charset.defaultCharset
import java.util.concurrent.ExecutorService
import kotlin.reflect.KClass

/**
 * Message handler that can reply messages to a reply queue.
 *
 * TODO Test content type support.
 */
internal class Handler<T : Any, R : Any> internal constructor (
    connectionFactory: ConnectionFactory,
    channel: Channel,
    private val executor: ExecutorService,
    private val type: KClass<T>,
    private val handler: (T) -> R) : DefaultConsumer(channel) {

    private companion object : Loggable {
        override val log: Logger = loggerOf<Handler<*, *>>()

        private const val RETRIES = 5
        private const val DELAY = 50L
    }

    private val client: RabbitMqClient by lazy { RabbitMqClient(connectionFactory) }

    /** @see DefaultConsumer.handleDelivery */
    override fun handleDelivery(
        consumerTag: String, envelope: Envelope, properties: BasicProperties, body: ByteArray) {

        executor.execute {
            val charset = properties.contentEncoding ?: defaultCharset().name()
            val correlationId = properties.correlationId
            val replyTo = properties.replyTo
            val contentType = formatsMap[properties.contentType] ?: defaultFormat

            try {
                trace("Received message ($correlationId) in $charset")
                val request = String(body, Charset.forName(charset))
                trace("Message body:\n$request")
                val input = request.parse(type, contentType)

                val response = handler(input)

                if (replyTo != null)
                    handleResponse(response, replyTo, correlationId)
            }
            catch (ex: Exception) {
                warn("Error processing message ($correlationId) in $charset", ex)

                if (replyTo != null)
                    handleError(ex, replyTo, correlationId)
            }
            finally {
                retry(Handler.Companion.RETRIES, Handler.Companion.DELAY) {
                    channel.basicAck(envelope.deliveryTag, false)
                }
            }
        }
    }

    private fun handleResponse(response: R, replyTo: String, correlationId: String?) {
        val output = when (response) {
            is String -> response
            is Int -> response.toString()
            is Long -> response.toString()
            else -> response.serialize()
        }

        client.publish(replyTo, output, correlationId)
    }

    private fun handleError(exception: Exception, replyTo: String, correlationId: String?) {
        val message = exception.message ?: ""
        val errorMessage = if (message.isBlank()) exception.javaClass.name else message
        client.publish(replyTo, errorMessage, correlationId)
    }
}
