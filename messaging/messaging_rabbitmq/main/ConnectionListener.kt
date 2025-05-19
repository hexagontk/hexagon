package com.hexagontk.messaging.rabbitmq

import com.hexagontk.core.debug
import com.hexagontk.core.loggerOf
import com.hexagontk.core.warn
import com.rabbitmq.client.*
import java.lang.System.Logger

internal class ConnectionListener : ShutdownListener, RecoveryListener {

    private val log: Logger = loggerOf(this::class)

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
