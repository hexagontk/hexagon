package co.there4.hexagon.log

import ch.qos.logback.classic.LoggerContext
import co.there4.hexagon.util.CompanionLogger
import co.there4.hexagon.util.Context
import org.slf4j.LoggerFactory.getILoggerFactory
import org.slf4j.LoggerFactory.getLogger
import org.slf4j.MDC
import org.testng.annotations.Test

/**
 * TODO Add asserts and check results (outputting logs to a buffer)
 */
@Test class DataAppenderTest {
    companion object : CompanionLogger (DataAppenderTest::class)

    val logger = getLogger(javaClass)

    fun reconfiguring_data_appender_takes_the_changes () {
        val lc = getILoggerFactory() as LoggerContext

        lc.loggerList.forEach {
            val appender = it.getAppender("data")

            if (appender != null && appender is DataAppender) {
                try {
                    appender.stop()
                    appender.start()

                    testAppender()

                    appender.stop()
                    appender.isFindCaller = false
                    appender.start()

                    testAppender()
                }
                catch (e: Exception) {
                    throw RuntimeException(e)
                }

            }
        }
    }

    private fun testAppender() {
        Context["foo"] = "bar"
        Context[0] = "str"
        MDC.put("var", "val")
        info ("info", mapOf ("p1" to "v1", "param" to "value"))
        info ("info", mapOf ("p1" to "v1", "param" to null))
        error("error", RuntimeException("runtime error"))
        logger.info("info2", "param")
        logger.info("noparam")
        logger.info("nullparam", null)
    }
}
