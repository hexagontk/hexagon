package co.there4.hexagon.events.rabbitmq

import co.there4.hexagon.events.Event
import co.there4.hexagon.events.EventManager
import org.testng.annotations.Test

@Test class RabbitMqEventEngineTest {
    data class Sample(val str: String, val int: Int) : Event()

    fun event_manager() {
        EventManager.engine = RabbitMqEventEngine()
        EventManager.consume(Sample::class) {
            if (it.str == "no message error")
                throw IllegalStateException()
            if (it.str == "message error")
                error("message")
        }
        EventManager.publish(Sample("foo", 1))
    }
}
