package co.there4.hexagon.log

import ch.qos.logback.classic.spi.LoggingEvent
import ch.qos.logback.classic.spi.ThrowableProxyUtil
import ch.qos.logback.core.UnsynchronizedAppenderBase
import co.there4.hexagon.util.Context
import co.there4.hexagon.util.hostname
import co.there4.hexagon.util.ip
import java.lang.management.ManagementFactory.getRuntimeMXBean
import java.util.*

/**
 *
 * Base appender for sending data to a destination.
 *
 * As the children classes will take care of the transport used to publish the event, this class's
 * responsability is only gathering common event data.
 *
 * @author juanjoaguililla
 */
abstract class DataAppender : UnsynchronizedAppenderBase<LoggingEvent>() {

    /**
     * Generates location information (class, method, etc.) for this appender. This will have a
     * performance penalty.
     *
     * @see .findCaller
     * @see .findCaller
     */
    var isFindCaller = false

    /**
     * Should take care of the connection to the platform to send events.
     *
     * @throws Exception thrown if any problem happends with the underlying transport technology.
     */
    protected fun open() {}

    /**
     * Responsible of cleaning up the state of the appender after use.
     *
     * @throws Exception thrown if any problem happends with the underlying transport technology.
     */
    protected fun close() {}

    /**
     * Should send the map with the events data to the event destination.
     *
     * @param data Event parameters.
     * @throws Exception thrown if any problem happends with the underlying transport technology.
     */
    @Throws(Exception::class)
    protected abstract fun write(data: Map<String, Any>)

    /**
     * It is overriden to make sure `super.start ()` is called.
     */
    override fun start() {
        super.start() // It is *IMPORTANT* to call super.start in first place
        open()
    }

    /**
     * Overriden to make sure `super.stop ()` is called.
     */
    override fun stop() {
        try {
            close()
        }
        finally {
            super.stop()
        }
    }

    /**
     * Overrides append and calls write. It transform the logging event into a map of properties.
     * It adds machine information and context information.
     *
     * @param loggingEvent The SLF4J framework log event.
     */
    override fun append(loggingEvent: LoggingEvent) {
        val data = LinkedHashMap<String, Any>()

        appendCommonData(loggingEvent, data)
        appendErrorData(loggingEvent, data)
        appendArgumentsData(loggingEvent, data)
        appendCallerData(loggingEvent, data)

        data.putAll(loggingEvent.mdcPropertyMap)
        data.putAll(Context.entries()
            .filter { it.key is String }
            .map { it.key as String to it.value }
        )

        // Call subclass write logic with all gathered information (removing 'null' values)
        write(data)
    }

    /**
     * If the arguments are a single map, its entries are added to the data.
     *
     * @param loggingEvent The SLF4J framework log event.
     * @param data Event parameters.
     */
    private fun appendArgumentsData(loggingEvent: LoggingEvent, data: MutableMap<String, Any>) {
        val arguments = loggingEvent.argumentArray
        if (arguments != null && arguments.size == 1 && arguments[0] is Map<*, *>) {
            val map = (arguments[0] as Map<*, *>)
                .filter { it.value != null }
                .mapKeys { it.key as String }
                .mapValues { it.value ?: throw IllegalStateException ("'null' values not allowed") }

            data.putAll(map)
        }
    }

    private fun appendCallerData(loggingEvent: LoggingEvent, data: MutableMap<String, Any>) {
        if (!isFindCaller)
            return

        val caller = loggingEvent.callerData[0]
        data.put("class", caller.className)
        data.put("method", caller.methodName)
        data.put("file", caller.fileName)
        data.put("line", caller.lineNumber)
    }

    private fun appendErrorData(loggingEvent: LoggingEvent, data: MutableMap<String, Any>) {
        val throwable = loggingEvent.throwableProxy
        if (throwable != null)
            data.put("exception", ThrowableProxyUtil.asString(throwable))
    }

    private fun appendCommonData(loggingEvent: LoggingEvent, data: MutableMap<String, Any>) {
        data.put("timestamp", loggingEvent.timeStamp)
        data.put("hostname", hostname)
        data.put("ip", ip)
        data.put("jvmid", getRuntimeMXBean().getName())
        data.put("thread", loggingEvent.threadName)
        data.put("component", loggingEvent.loggerName)
        data.put("message", loggingEvent.formattedMessage)
        data.put("level", loggingEvent.level.levelStr)
    }
}
