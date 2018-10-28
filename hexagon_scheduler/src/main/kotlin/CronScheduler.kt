package com.hexagonkt.scheduler

import com.hexagonkt.helpers.error
import com.cronutils.model.CronType.QUARTZ
import com.cronutils.model.definition.CronDefinitionBuilder.instanceDefinitionFor as cronDefinition
import com.cronutils.model.time.ExecutionTime
import com.cronutils.parser.CronParser
import com.hexagonkt.helpers.Logger
import com.hexagonkt.helpers.logger

import java.lang.Runtime.getRuntime
import java.time.ZonedDateTime
import java.util.concurrent.ScheduledThreadPoolExecutor
import java.util.concurrent.TimeUnit.SECONDS

/**
 * Scheduler for processes. After using it, you should call the `shutdown` method.
 */
class CronScheduler(threads: Int = getRuntime().availableProcessors()) {
    private val log: Logger = logger()

    private val scheduler = ScheduledThreadPoolExecutor(threads)
    private val cronParser = CronParser(cronDefinition (QUARTZ))

    fun schedule (cronExpression: String, callback: () -> Unit) {
        val cron = cronParser.parse (cronExpression)
        val cronExecution = ExecutionTime.forCron(cron)

        scheduler.schedule ({ function (callback, cronExecution) }, delay(cronExecution), SECONDS)
    }

    fun shutdown () { scheduler.shutdown() }

    private fun delay(cronExecution: ExecutionTime): Long =
        cronExecution.timeToNextExecution(ZonedDateTime.now ()).orElseThrow { error }.seconds

    private fun function(callback: () -> Unit, cronExecution: ExecutionTime) {
        try {
            callback()
        }
        catch (e: Exception) {
            log.error({ "Error executing cron job" }, e)
        }

        scheduler.schedule ({ function (callback, cronExecution) }, delay(cronExecution), SECONDS)
    }
}
