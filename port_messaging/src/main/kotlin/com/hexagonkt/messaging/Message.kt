package com.hexagonkt.messaging

import java.lang.System.currentTimeMillis
import java.lang.Thread.currentThread
import java.time.LocalDateTime

import com.hexagonkt.helpers.asNumber
import com.hexagonkt.helpers.Environment

open class Message (
    val timestamp: Long = currentTimeMillis (),
    val dateTime: Long = LocalDateTime.now().asNumber(),
    val hostname: String = Environment.hostname,
    val ip: String = Environment.ip,
    val jvmid: String = Environment.jvmId,
    val thread: String = currentThread ().name
)
