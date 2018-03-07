package com.hexagonkt.scheduler

import com.hexagonkt.helpers.error
import com.cronutils.model.CronType.QUARTZ
import com.cronutils.model.definition.CronDefinitionBuilder.instanceDefinitionFor as cronDefinition
import com.cronutils.model.time.ExecutionTime
import com.cronutils.parser.CronParser
import com.hexagonkt.helpers.Loggable
import com.hexagonkt.helpers.loggerOf
import org.slf4j.Logger
import org.threeten.bp.ZonedDateTime
import java.lang.Runtime.getRuntime
import java.util.concurrent.ScheduledThreadPoolExecutor
import java.util.concurrent.TimeUnit.SECONDS

class CronScheduler(threads: Int = getRuntime().availableProcessors()) {
    companion object : Loggable {
        override val log: Logger = loggerOf<CronScheduler>()
    }

    private val scheduler = ScheduledThreadPoolExecutor(threads)
    private val cronParser = CronParser(cronDefinition (QUARTZ))

    fun schedule (cronExpression: String, callback: () -> Unit) {
        val cron = cronParser.parse (cronExpression)
        val cronExecution = ExecutionTime.forCron(cron)

        scheduler.schedule ({ function (callback, cronExecution) }, delay(cronExecution), SECONDS)
    }

    fun shutdown () { scheduler.shutdown() }

    private fun delay(cronExecution: ExecutionTime) =
        cronExecution.timeToNextExecution(ZonedDateTime.now ()).orNull()?.seconds ?: error

    private fun function(callback: () -> Unit, cronExecution: ExecutionTime) {
        try {
            callback()
        }
        catch (e: Exception) {
            fail("Error executing cron job", e)
        }

        scheduler.schedule ({ function (callback, cronExecution) }, delay(cronExecution), SECONDS)
    }
}
