package co.there4.hexagon.events

import co.there4.hexagon.events.EventManager.consume
import co.there4.hexagon.events.EventManager.publish
import co.there4.hexagon.util.Loggable
import co.there4.hexagon.util.caller
import org.testng.annotations.AfterClass
import org.testng.annotations.BeforeClass
import org.testng.annotations.Test
import java.lang.System.nanoTime
import java.lang.Thread.`yield` as threadYield

@Test class EventsTest : Loggable {
    class TickEvent (val nanos: Long) : Event ()

    private var tick: Long = 0

    @BeforeClass fun startConsumer() {
        consume(TickEvent::class) {
            info("Tick: ${it.nanos}")
            tick = it.nanos
        }
    }

    @AfterClass fun deleteTestQueue() {
        val backend = EventManager.backend
        if (backend is RabbitMqEventBackend)
            backend.client.deleteQueue(TickEvent::class.java.name)
    }

    fun events_are_published_properly() {
        val nanos = nanoTime()
        publish(TickEvent(nanos))

        // Wait for the consumer to handle the event
        while (tick == 0L)
            threadYield()

        assert(tick == nanos)
    }

    fun events_location() {
        val cl = caller()
        assert(caller().contains("events_location"))
        assert(cl.contains("events_location"))
    }
}
