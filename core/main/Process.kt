package com.hexagontk.core

import java.lang.management.ManagementFactory.getRuntimeMXBean
import java.lang.management.RuntimeMXBean

/**
 * Object with utilities to gather information about the running process.
 */
object Process {
    private val runtimeBean: RuntimeMXBean by lazy { getRuntimeMXBean() }

    /** OS Process ID. */
    val pid: Long by lazy { runtimeBean.pid }

    /**
     * Milliseconds since the process was created.
     *
     * @return Milliseconds since the process was created.
     */
    fun uptime(): Long =
        runtimeBean.uptime
}
