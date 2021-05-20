package com.hexagonkt.scheduler

import org.junit.jupiter.api.Test
import java.lang.Thread.sleep

internal class CronSchedulerTest {

    @Test fun `Callback is executed properly`() {
        val cron = CronScheduler()
        val times = 1
        var count = 0

        cron.schedule("0/1 * * * * ?") {
            count++
        }

        sleep((times * 1_000) + 100L)
        cron.shutdown()
        assert(count == times)
    }

    @Test fun `Failing callback does not prevent future executions`() {
        val cron = CronScheduler()
        val times = 2
        var count = 0

        cron.schedule("0/1 * * * * ?") {
            count++
            error("Error in cron job")
        }

        sleep((times * 1_000) + 200L)
        cron.shutdown()
        assert(count == times)
    }
}
