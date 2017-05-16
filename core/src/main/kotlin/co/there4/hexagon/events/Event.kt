package co.there4.hexagon.events

import java.lang.System.currentTimeMillis
import java.lang.Thread.currentThread
import java.time.LocalDateTime

import co.there4.hexagon.helpers.asNumber
import co.there4.hexagon.helpers.hostname as utilHostname
import co.there4.hexagon.helpers.ip as utilIp
import co.there4.hexagon.helpers.jvmId as utilJvmId

open class Event (
    val timestamp: Long = currentTimeMillis (),
    val dateTime: Long = LocalDateTime.now().asNumber(),
    val hostname: String = utilHostname,
    val ip: String = utilIp,
    val jvmid: String = utilJvmId,
    val thread: String = currentThread ().name
)
