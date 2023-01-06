package com.hexagonkt.http.server.jetty

import org.eclipse.jetty.util.VirtualThreads.getDefaultVirtualThreadsExecutor
import org.eclipse.jetty.util.thread.ThreadPool
import java.util.concurrent.ExecutorService
import java.util.concurrent.TimeUnit.NANOSECONDS
import kotlin.Long.Companion.MAX_VALUE

internal class VirtualThreadPool : ThreadPool {
    private var executorService: ExecutorService =
        getDefaultVirtualThreadsExecutor() as ExecutorService

    override fun execute(command: Runnable) {
        executorService.submit(command)
    }

    override fun join() {
        executorService.awaitTermination(MAX_VALUE, NANOSECONDS)
    }

    override fun getThreads(): Int = 1
    override fun getIdleThreads(): Int = 1
    override fun isLowOnThreads(): Boolean = false
}
