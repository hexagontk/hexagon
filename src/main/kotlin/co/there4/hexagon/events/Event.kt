package co.there4.hexagon.events

import co.there4.hexagon.util.Context
import co.there4.hexagon.util.asNumber
import java.lang.System.currentTimeMillis
import java.lang.Thread.currentThread
import java.time.LocalDateTime
import co.there4.hexagon.util.hostname as utilHostname
import co.there4.hexagon.util.ip as utilIp
import co.there4.hexagon.util.jvmId as utilJvmId

open class Event (
    val action: String,
    val timestamp: Long = currentTimeMillis (),
    val dateTime: Long = LocalDateTime.now().asNumber(),
    val hostname: String = utilHostname,
    val ip: String = utilIp,
    val jvmid: String = utilJvmId,
    val thread: String = currentThread ().name,
    val location: String = currentThread ().stackTrace[3].toString (),
    val context: Map<String, Any> = Context.entries()
        .filter { it.key is String && it.value is String }
        .map { it.key as String to it.value }
        .toMap()
)
