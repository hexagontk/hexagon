package com.hexagonkt.messaging

import java.lang.System.currentTimeMillis
import java.lang.Thread.currentThread
import java.time.LocalDateTime

import com.hexagonkt.helpers.asNumber
import com.hexagonkt.helpers.Environment.hostname as utilHostname
import com.hexagonkt.helpers.Environment.ip as utilIp
import com.hexagonkt.helpers.Environment.jvmId as utilJvmId

open class Message (
    val timestamp: Long = currentTimeMillis (),
    val dateTime: Long = LocalDateTime.now().asNumber(),
    val hostname: String = utilHostname,
    val ip: String = utilIp,
    val jvmid: String = utilJvmId,
    val thread: String = currentThread ().name
)
