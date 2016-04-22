package co.there4.hexagon.events

import co.there4.hexagon.util.CompanionLogger
import org.testng.annotations.AfterClass
import org.testng.annotations.BeforeClass
import org.testng.annotations.Test
import java.lang.System.nanoTime

class EventsDemo {
    companion object : CompanionLogger(EventsDemo::class)

    private val events = EventManager()

    @BeforeClass fun startConsumer() {
        events.on(TickEvent::class, "clock.tick") { a -> info("Tick: ${a.nanos}") }
    }

    @AfterClass fun deleteTestQueue() {
        events.client.deleteQueue("clock.tick")
    }

    @Test(threadPoolSize = 8, invocationCount = 50)
    fun events_are_published_properly() {
        events.publish(TickEvent(nanoTime()))
    }
}
