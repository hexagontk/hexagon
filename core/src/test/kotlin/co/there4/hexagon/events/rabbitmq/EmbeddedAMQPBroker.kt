package co.there4.hexagon.events.rabbitmq

import co.there4.hexagon.helpers.resource
import org.apache.qpid.server.Broker
import org.apache.qpid.server.BrokerOptions
import java.nio.file.Files

/**
 * Check: https://dzone.com/articles/mocking-rabbitmq-for-integration-tests
 */
class EmbeddedAMQPBroker(val port: Int, val user: String, val password: String, val vhost: String) {
    private var broker: Broker? = null

    fun startup() {
        val preferenceStore = """{"type": "Noop"}"""
        val workDir = Files.createTempDirectory("qpid").toFile().absolutePath
        val options = BrokerOptions()

        options.isStartupLoggedToSystemOut = false
        options.initialConfigurationLocation = resource("qpid.json").toString()
        options.configurationStoreType = "Memory"

        options.setConfigProperty("qpid.broker.defaultPreferenceStoreAttributes", preferenceStore)
        options.setConfigProperty("qpid.amqp_port", port.toString())
        options.setConfigProperty("qpid.user", user)
        options.setConfigProperty("qpid.password", password)
        options.setConfigProperty("qpid.vhost", vhost)
        options.setConfigProperty("qpid.work_dir", workDir)

        broker = Broker()
        broker?.startup(options)
    }

    fun shutdown() { broker?.shutdown() }
}
