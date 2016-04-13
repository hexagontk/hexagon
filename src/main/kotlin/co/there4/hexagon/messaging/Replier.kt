package co.there4.hexagon.messaging

import co.there4.hexagon.serialization.serialize
import com.rabbitmq.client.Channel
import com.rabbitmq.client.ConnectionFactory
import java.util.concurrent.ExecutorService
import kotlin.reflect.KClass

class Replier<T : Any, R : Any> (
    connectionFactory: ConnectionFactory,
    channel: Channel,
    executor: ExecutorService,
    type: KClass<T>,
    val handler: (T) -> R) : Handler<T> (channel, executor, type) {

    private val client: RabbitClient = RabbitClient (connectionFactory)

    override fun handleMessage(message: T, replyTo: String?, correlationId: String?) {
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

    override fun handleError(exception: Exception, replyTo: String?, correlationId: String?) {
        if (replyTo != null && correlationId != null)
            client.publish("", replyTo, exception.message ?: exception.javaClass.name, correlationId)
    }
}
