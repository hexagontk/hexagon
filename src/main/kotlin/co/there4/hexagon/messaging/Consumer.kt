package co.there4.hexagon.messaging

import com.rabbitmq.client.Channel
import java.util.concurrent.ExecutorService
import kotlin.reflect.KClass

class Consumer<T : Any> (
    channel: Channel,
    executor: ExecutorService,
    type: KClass<T>,
    val handler: (T) -> Unit) : Handler<T> (channel, executor, type) {

    override fun handleMessage(input: T, replyTo: String?, correlationId: String?) = handler (input)
}
