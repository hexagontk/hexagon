package com.hexagonkt.messaging.rabbitmq

import com.hexagonkt.helpers.resource
import org.apache.qpid.server.Broker
import org.apache.qpid.server.BrokerOptions
import java.nio.file.Files.createTempDirectory

internal class EmbeddedAMQPBroker(
    private val port: Int,
    private val user: String,
    private val password: String,
    private val vhost: String) {

    private val broker: Broker = Broker()

    internal fun startup() {
        val options = with(BrokerOptions()) {
            isStartupLoggedToSystemOut = false
            initialConfigurationLocation = resource("qpid.json").toString()
            configurationStoreType = "Memory"

            val storeAttributes = """{"type": "Noop"}"""
            setConfigProperty("qpid.broker.defaultPreferenceStoreAttributes", storeAttributes)
            setConfigProperty("qpid.amqp_port", port.toString())
            setConfigProperty("qpid.user", user)
            setConfigProperty("qpid.password", password)
            setConfigProperty("qpid.vhost", vhost)
            setConfigProperty("qpid.work_dir", createTempDirectory("qpid").toFile().absolutePath)
            this
        }

        broker.startup(options)
    }

    internal fun shutdown() { broker.shutdown() }
}
