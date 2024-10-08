package com.hexagonkt.scheduler

import kotlin.test.Test
import java.lang.Thread.sleep
import kotlin.test.assertEquals

internal class CronSchedulerTest {

    @Test fun callbackExecutedProperly() {
        // sample
        val cron = CronScheduler()
        val times = 1
        var count = 0

        // Increments the counter by one each second
        cron.schedule("0/1 * * * * ?") {
            count++
        }

        sleep((times * 1_000) + 200L)
        cron.shutdown()
        assertEquals(count, times)
        // sample
    }

    @Test fun `Failing callback does not prevent future executions`() {
        val cron = CronScheduler()
        val times = 2
        var count = 0

        cron.schedule("0/1 * * * * ?") {
            count++
            error("Error in cron job")
        }

        sleep((times * 1_000) + 400L)
        cron.shutdown()
        assertEquals(count, times)
    }
}
