package com.hexagontk.messaging

import java.lang.System.currentTimeMillis
import java.lang.Thread.currentThread
import java.time.LocalDateTime

import com.hexagontk.core.Platform

open class Message (
    val timestamp: Long = currentTimeMillis(),
    val dateTime: LocalDateTime = LocalDateTime.now(),
    val hostname: String = Platform.hostName,
    val ip: String = Platform.ip,
//    val jvmId: String = Jvm.id,
    val thread: String = currentThread().name
)
