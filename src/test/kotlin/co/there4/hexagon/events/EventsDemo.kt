package co.there4.hexagon.events

import co.there4.hexagon.util.CompanionLogger
import org.testng.annotations.AfterClass
import org.testng.annotations.BeforeClass
import org.testng.annotations.Test
import java.lang.System.nanoTime

@Test class EventsDemo {
    companion object : CompanionLogger(EventsDemo::class)

    @BeforeClass fun startConsumer() {
        EventManager.on(TickEvent::class, "clock.tick") { a -> info("Tick: ${a.nanos}") }
    }

    @AfterClass fun deleteTestQueue() {
        EventManager.client.deleteQueue("clock.tick")
    }

    fun events_are_published_properly() {
        EventManager.publish(TickEvent(nanoTime()))
    }
}
