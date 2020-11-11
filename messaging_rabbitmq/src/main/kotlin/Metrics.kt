package com.hexagonkt.messaging.rabbitmq

import com.codahale.metrics.ConsoleReporter
import com.codahale.metrics.Meter
import com.codahale.metrics.MetricRegistry
import com.codahale.metrics.jmx.JmxReporter
import com.hexagonkt.logging.Logger
import com.rabbitmq.client.MetricsCollector
import com.rabbitmq.client.impl.StandardMetricsCollector
import java.util.concurrent.TimeUnit


internal class Metrics(private val metrics: StandardMetricsCollector) {

    private val log: Logger = Logger(this::class)
    private val reg: MetricRegistry = metrics.metricRegistry

    fun setJmxReporter() {
        val reporter = JmxReporter.forRegistry(reg)
            .inDomain("com.rabbitmq.client.jmx")
            .convertRatesTo(TimeUnit.SECONDS)
            .convertDurationsTo(TimeUnit.MILLISECONDS)
            .build()
        reporter.start()
    }

    fun setConsoleReporter() {
        val reporter: ConsoleReporter = ConsoleReporter.forRegistry(reg)
            .convertRatesTo(TimeUnit.SECONDS)
            .convertDurationsTo(TimeUnit.MILLISECONDS)
            .build()
        reporter.start(1, TimeUnit.SECONDS)
    }

    fun report() {
        val publishedMessagesCount: Meter? = metrics.publishedMessages
        val consumedMessagesCount: Meter? = metrics.consumedMessages
        val acknowledgedMessagesCount: Meter? = metrics.acknowledgedMessages
        val rejectedMessagesCount: Meter? = metrics.rejectedMessages
        val failedToPublishMessagesCount: Meter? = metrics.failedToPublishMessages

        log.debug { "Number of published messages ${publishedMessagesCount?.count}" }
        log.debug { "Number of consumed messages ${consumedMessagesCount?.count}" }
        log.debug { "Number of acknowledged messages ${acknowledgedMessagesCount?.count}" }
        log.debug { "Number of rejected messages ${rejectedMessagesCount?.count}" }
        log.debug { "Number of failed to publish messages ${failedToPublishMessagesCount?.count}" }
    }
}
