---
layout: site_page
title: Events
date: 2016-04-13 15:07
published: true
---

Events
======

```
    import com.hexagonkt.helpers.CompanionLogger
    import org.testng.annotations.AfterClass
    import org.testng.annotations.BeforeClass
    import org.testng.annotations.Test
    import java.lang.System.nanoTime
    import java.lang.Thread.`yield` as threadYield

    @Test class EventsDemo {
        class TickEvent (val nanos: Long) : Event (TickEvent::class.java.name)

        companion object : CompanionLogger(EventsDemo::class)

        private var tick: Long = 0

        @BeforeClass fun startConsumer() {
            EventManager.on(TickEvent::class) {
                info("Tick: ${it.nanos}")
                tick = it.nanos
            }
        }

        @AfterClass fun deleteTestQueue() {
            EventManager.client.deleteQueue(TickEvent::class.java.name)
        }

        fun events_are_published_properly() {
            val nanos = nanoTime()
            EventManager.publish(TickEvent(nanos))

            // Wait for the consumer to handle the event
            while (tick == 0L)
                threadYield()

            assert(tick == nanos)
        }
    }
```
