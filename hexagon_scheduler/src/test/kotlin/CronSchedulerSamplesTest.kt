package com.hexagonkt.scheduler

import org.testng.annotations.Test
import java.lang.Thread.sleep

@Test class CronSchedulerSamplesTest {

    @Test fun callbackExecutedProperly() { // sample
        val cron = CronScheduler()
        val times = 1
        var count = 0

        cron.schedule("0/1 * * * * ?") {
            count++
        }

        sleep((times * 1_000) + 100L)
        cron.shutdown()
        assert(count == times)
    } // sample
}
