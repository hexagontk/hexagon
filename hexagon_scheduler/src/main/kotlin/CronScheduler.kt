package com.hexagonkt.scheduler

import com.hexagonkt.helpers.error
import com.cronutils.model.CronType.QUARTZ
import com.cronutils.model.definition.CronDefinitionBuilder.instanceDefinitionFor as cronDefinition
import com.cronutils.model.time.ExecutionTime
import com.cronutils.parser.CronParser
import com.hexagonkt.helpers.Logger

import java.lang.Runtime.getRuntime
import java.time.ZonedDateTime
import java.util.concurrent.ScheduledThreadPoolExecutor
import java.util.concurrent.TimeUnit.SECONDS

/**
 * Scheduler to execute tasks repeatedly. After using it, you should call the [shutdown] method. If
 * the JVM finishes without calling [shutdown], it will be called upon JVM termination.
 *
 * @param threads Number of threads used by the thread pool. By default it is equals to the number
 *  of processors.
 *
 * @sample com.hexagonkt.scheduler.CronSchedulerSamplesTest.callbackExecutedProperly
 */
class CronScheduler(threads: Int = getRuntime().availableProcessors()) {
    private val log: Logger = Logger(this)

    private val scheduler = ScheduledThreadPoolExecutor(threads)
    private val cronParser = CronParser(cronDefinition(QUARTZ))

    init {
        getRuntime().addShutdownHook(Thread { shutdown() })
    }

    /**
     * Schedules a block of code to be executed repeatedly by a
     * [Cron](https://en.wikipedia.org/wiki/Cron) expresion.
     *
     * @param cronExpression Periodicity of the task in Cron format.
     * @param callback Task code to be executed periodically.
     */
    fun schedule(cronExpression: String, callback: () -> Unit) {
        val cron = cronParser.parse(cronExpression)
        val cronExecution = ExecutionTime.forCron(cron)

        scheduler.schedule({ function(callback, cronExecution) }, delay(cronExecution), SECONDS)
    }

    /**
     * Shuts down this scheduler's thread pool. Calling over an already closed scheduler does not
     * have any effect. It is called by the JVM when it is shut down.
     */
    fun shutdown() {
        scheduler.shutdown()
    }

    private fun delay(cronExecution: ExecutionTime): Long =
        cronExecution.timeToNextExecution(ZonedDateTime.now()).orElseThrow { error }.seconds

    private fun function(callback: () -> Unit, cronExecution: ExecutionTime) {
        try {
            callback()
        }
        catch (e: Exception) {
            log.error(e) { "Error executing cron job" }
        }

        scheduler.schedule({ function(callback, cronExecution) }, delay(cronExecution), SECONDS)
    }
}
