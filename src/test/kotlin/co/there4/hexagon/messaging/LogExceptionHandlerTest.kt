package co.there4.hexagon.messaging

import com.rabbitmq.client.Channel
import com.rabbitmq.client.Connection
import com.rabbitmq.client.TopologyRecoveryException
import org.mockito.Mockito.`when` as mockOn
import org.mockito.Mockito.mock
import org.mockito.invocation.InvocationOnMock
import org.testng.annotations.Test
import java.net.InetAddress.getLocalHost

@Test class LogExceptionHandlerTest {
    val handler = LogExceptionHandler()

    private fun <T, R> on(call: T, lambda: (InvocationOnMock) -> R) {
        mockOn(call).then (lambda)
    }

    fun test_error_logging_methods() {
        val con = mock(Connection::class.java)
        on(con.address) { getLocalHost() }

        val ch = mock(Channel::class.java)

        val ex = RuntimeException()
        handler.handleUnexpectedConnectionDriverException(con, ex)
        handler.handleReturnListenerException(ch, ex)
        handler.handleFlowListenerException(ch, ex)
        handler.handleConfirmListenerException(ch, ex)
        handler.handleBlockedListenerException(con, ex)

        handler.handleConsumerException(ch, ex, null, null, null)

        handler.handleConnectionRecoveryException(con, ex)
        handler.handleChannelRecoveryException(ch, ex)
        handler.handleTopologyRecoveryException(con, ch, TopologyRecoveryException("", ex))
    }
}
