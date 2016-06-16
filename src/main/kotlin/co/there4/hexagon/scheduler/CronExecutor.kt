package co.there4.hexagon.scheduler

import co.there4.hexagon.util.CompanionLogger
import com.cronutils.model.CronType.QUARTZ
import com.cronutils.model.definition.CronDefinitionBuilder.instanceDefinitionFor as cronDefinition
import com.cronutils.model.time.ExecutionTime
import com.cronutils.parser.CronParser
import org.joda.time.DateTime
import java.lang.Runtime.getRuntime
import java.util.concurrent.ScheduledThreadPoolExecutor
import java.util.concurrent.TimeUnit.SECONDS

class CronExecutor (threads: Int = getRuntime().availableProcessors()) {
    companion object : CompanionLogger (CronExecutor::class)

    private val scheduler = ScheduledThreadPoolExecutor(threads)
    private val cronParser = CronParser(cronDefinition (QUARTZ))

    fun schedule (cronExpression: String, callback: () -> Unit) {
        val cron = cronParser.parse (cronExpression)
        val cronExecution = ExecutionTime.forCron(cron)
        val nextExecution = cronExecution.timeToNextExecution(DateTime.now ())
        val nextExecutionSeconds = nextExecution.standardSeconds

        scheduler.schedule ({ function (callback, cronExecution) }, nextExecutionSeconds, SECONDS)
    }

    fun shutdown () { scheduler.shutdown() }

    private fun function(callback: () -> Unit, cronExecution: ExecutionTime) {
        try {
            callback()
        }
        catch (e: Exception) {
            // TODO Fire an event
            err ("Error executing cron job", e)
        }

        val nextExecution = cronExecution.timeToNextExecution(DateTime.now ())
        val nextExecutionSeconds = nextExecution.standardSeconds

        scheduler.schedule ({ function (callback, cronExecution) }, nextExecutionSeconds, SECONDS)
    }
}
