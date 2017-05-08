package co.there4.hexagon.events.rabbitmq

import org.apache.qpid.server.Broker
import org.apache.qpid.server.BrokerOptions
import java.nio.file.Files
import java.nio.file.Paths

/**
 * Check: https://dzone.com/articles/mocking-rabbitmq-for-integration-tests
 */
class EmbeddedAMQPBroker {
    private var broker: Broker? = null

    fun startup() {
        val dir = Paths.get("src/test/resources").toAbsolutePath().toFile().absolutePath
        val workDir = Files.createTempDirectory("derby")
        val brokerOptions = BrokerOptions()

        brokerOptions.setConfigProperty("qpid.amqp_port", "5673")
        brokerOptions.setConfigProperty("qpid.work_dir", workDir.toFile().absolutePath)
        brokerOptions.initialConfigurationLocation = "$dir/qpid.json"
        broker = Broker()
        broker?.startup(brokerOptions)
    }

    fun shutdown() { broker?.shutdown() }
}
