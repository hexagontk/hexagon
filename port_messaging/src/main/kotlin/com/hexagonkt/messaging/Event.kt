package com.hexagonkt.messaging

import java.lang.System.currentTimeMillis
import java.lang.Thread.currentThread
import java.time.LocalDateTime

import com.hexagonkt.helpers.asNumber
import com.hexagonkt.helpers.hostname as utilHostname
import com.hexagonkt.helpers.ip as utilIp
import com.hexagonkt.helpers.jvmId as utilJvmId

open class Event (
    val timestamp: Long = currentTimeMillis (),
    val dateTime: Long = LocalDateTime.now().asNumber(),
    val hostname: String = utilHostname,
    val ip: String = utilIp,
    val jvmid: String = utilJvmId,
    val thread: String = currentThread ().name
)
