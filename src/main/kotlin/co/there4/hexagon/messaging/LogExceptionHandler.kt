package co.there4.hexagon.messaging

import co.there4.hexagon.util.CompanionLogger
import com.rabbitmq.client.Channel
import com.rabbitmq.client.Connection
import com.rabbitmq.client.Consumer
import com.rabbitmq.client.TopologyRecoveryException
import com.rabbitmq.client.impl.DefaultExceptionHandler

class LogExceptionHandler : DefaultExceptionHandler () {
    companion object : CompanionLogger (LogExceptionHandler::class)

    private fun logError(error: String, con: Connection, ex: Throwable) {
        err("%s in connection to: %s".format(ex, error, con.address.canonicalHostName))
    }

    private fun logError(error: String, ch: Channel, ex: Throwable) {
        err("%s in channel: %s".format(ex, error, ch.channelNumber))
    }

    private fun logError(error: String, con: Connection, ch: Channel, ex: Throwable) {
        val message = "%s in connection to: %s in channel: %s"
            .format(error, con.address.canonicalHostName, ch.channelNumber)

        err(message, ex)
    }

    override fun handleUnexpectedConnectionDriverException(con: Connection, ex: Throwable) {
        super.handleUnexpectedConnectionDriverException(con, ex)
        logError("UnexpectedConnectionDriverException", con, ex)
    }

    override fun handleReturnListenerException(ch: Channel, ex: Throwable) {
        super.handleReturnListenerException(ch, ex)
        logError("ReturnListenerException", ch, ex)
    }

    override fun handleFlowListenerException(ch: Channel, ex: Throwable) {
        super.handleFlowListenerException(ch, ex)
        logError("FlowListenerException", ch, ex)
    }

    override fun handleConfirmListenerException(ch: Channel, ex: Throwable) {
        super.handleConfirmListenerException(ch, ex)
        logError("ConfirmListenerException", ch, ex)
    }

    override fun handleBlockedListenerException(con: Connection, ex: Throwable) {
        super.handleBlockedListenerException(con, ex)
        logError("BlockedListenerException", con, ex)
    }

    override fun handleConsumerException(
        ch: Channel,
        ex: Throwable,
        consumer: Consumer?,
        consumerTag: String?,
        methodName: String?) {

        super.handleConsumerException(ch, ex, consumer, consumerTag, methodName)

        val message = "ConsumerException in channel: %s consumer: %s method: %s"
            .format(ch.channelNumber, consumerTag, methodName)

        err(message, ex)
    }

    override fun handleConnectionRecoveryException(con: Connection, ex: Throwable) {
        super.handleConnectionRecoveryException(con, ex)
        logError("ConnectionRecoveryException", con, ex)
    }

    override fun handleChannelRecoveryException(ch: Channel, ex: Throwable) {
        super.handleChannelRecoveryException(ch, ex)
        logError("ChannelRecoveryException", ch, ex)
    }

    override fun handleTopologyRecoveryException(
        con: Connection, ch: Channel, ex: TopologyRecoveryException) {

        super.handleTopologyRecoveryException(con, ch, ex)
        logError("TopologyRecoveryException", con, ch, ex)
    }
}
