package co.there4.hexagon.events

import co.there4.hexagon.events.EventManager.consume
import co.there4.hexagon.events.EventManager.publish
import co.there4.hexagon.util.CompanionLogger
import co.there4.hexagon.util.caller
import org.testng.annotations.AfterClass
import org.testng.annotations.BeforeClass
import org.testng.annotations.Test
import java.lang.System.nanoTime
import java.lang.Thread.`yield` as threadYield

@Test class EventsTest {
    class TickEvent (val nanos: Long) : Event (TickEvent::class.java.name)

    companion object : CompanionLogger(EventsTest::class)

    private var tick: Long = 0

    @BeforeClass fun startConsumer() {
        consume(TickEvent::class) {
            info("Tick: ${it.nanos}")
            tick = it.nanos
        }
    }

    @AfterClass fun deleteTestQueue() {
        EventManager.client.deleteQueue(TickEvent::class.java.name)
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
        assert(Event("action").location.contains("events_location"))
        val cl = caller()
        assert(caller().contains("events_location"))
        assert(cl.contains("events_location"))
    }
}
