package com.hexagonkt.scheduler

import org.testng.annotations.Test
import java.lang.Thread.sleep

@Test class CronSchedulerTest {
    fun callback_is_executed_properly () {
        val cron = CronScheduler()
        val times = 1
        var count = 0

        cron.schedule("0/1 * * * * ?") { count++ }

        sleep ((times * 1000) + 100L)
        cron.shutdown()
        assert (count == times)
    }

    fun failing_callback_does_not_prevent_future_executions () {
        val cron = CronScheduler()
        val times = 2
        var count = 0

        cron.schedule("0/1 * * * * ?") {
            count++
            throw RuntimeException ("Error in cron job")
        }

        sleep ((times * 1000) + 100L)
        cron.shutdown()
        assert (count == times)
    }
}
