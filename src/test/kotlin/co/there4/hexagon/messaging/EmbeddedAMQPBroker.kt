package co.there4.hexagon.messaging

import com.google.common.io.Files
import org.apache.qpid.server.Broker
import org.apache.qpid.server.BrokerOptions
import java.io.File

/**
 * Check: https://dzone.com/articles/mocking-rabbitmq-for-integration-tests
 */
class EmbeddedAMQPBroker {
    private val dir = File(".").absolutePath + "/src/test/resources"
    private val BROKER_PORT = 5673
    private val broker = Broker()
    private val brokerOptions = BrokerOptions()

    init {
        brokerOptions.setConfigProperty("qpid.amqp_port", BROKER_PORT.toString())
        brokerOptions.setConfigProperty("qpid.pass_file", "$dir/passwd.txt")
        brokerOptions.setConfigProperty("qpid.work_dir", Files.createTempDir().getAbsolutePath())
        brokerOptions.setInitialConfigurationLocation("$dir/qpid.json")
    }

    fun startup() { broker.startup(brokerOptions) }
    fun shutdown() { broker.shutdown() }
}
