package com.hexagonkt.messaging.rabbitmq

import com.hexagonkt.logging.Logger
import com.rabbitmq.client.*

internal class ConnectionListener : ShutdownListener, RecoveryListener {

    private val log: Logger = Logger(this::class)

    /** @see ShutdownListener.shutdownCompleted */
    override fun shutdownCompleted(cause: ShutdownSignalException) {
        if (cause.isHardError) { // connection error
            if (!cause.isInitiatedByApplication)
                log.warn { "Connection shutdown was caused by broker " + cause.reason }
            else
                log.warn { "Connection shutdown is initiated by application. Ignoring it" }
        }
        else { // channel error
            if (!cause.isInitiatedByApplication) {
                val channel: Channel = cause.reference as Channel
                log.warn { "Channel shutdown was caused by broker " + channel.closeReason }
            }
            else
                log.warn { "Channel shutdown is initiated by application. Ignoring it" }
        }
    }

    /** @see RecoveryListener.handleRecovery */
    override fun handleRecovery(recoverable: Recoverable) {
        if (recoverable is Connection)
            log.debug { "Connection was recovered" }
        else if (recoverable is Channel)
            log.debug { "Connection to channel #" + recoverable.channelNumber + " was recovered" }
    }

    /** @see RecoveryListener.handleRecoveryStarted */
    override fun handleRecoveryStarted(recoverable: Recoverable) {
        log.debug { "Automatic connection recovery starts" }
    }
}
