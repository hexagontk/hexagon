package co.there4.hexagon.scheduling

import org.testng.annotations.Test

@Test class CronExecutorTest {
    private var count = 0

    fun failing_callback_does_not_prevent_future_executions () {
        val cron = CronExecutor ()
        val times = 2

        cron.schedule("0/1 * * * * *") {
            count++
            throw RuntimeException ("Error in cron job")
        }

        Thread.sleep ((times * 1000) + 100L)
        cron.shutdown()
        assert (count == 2)
    }
}
