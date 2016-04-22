package co.there4.hexagon.scheduling

import co.there4.hexagon.util.CompanionLogger
import com.cronutils.model.CronType
import com.cronutils.model.definition.CronDefinitionBuilder
import com.cronutils.parser.CronParser
import java.lang.Thread.sleep
import java.util.concurrent.ScheduledThreadPoolExecutor
import java.util.concurrent.TimeUnit

class CronExecutor {
    companion object : CompanionLogger (CronExecutor::class)

    internal val s = ScheduledThreadPoolExecutor(8)

    init {
        val cronDefinition =
        CronDefinitionBuilder.instanceDefinitionFor (CronType.UNIX)

        //create a parser based on provided definition
        val parser = CronParser(cronDefinition)
        val quartzCron = parser.parse ("0 23 ? * * 1-5 *")

        info ("start")
        s.scheduleAtFixedRate (
            {
                info ("Begin Execution")
                sleep (1000)
                info ("End Execution")
            },
            2, 5, TimeUnit.SECONDS)
    }
}
