package co.there4.hexagon.events

import co.there4.hexagon.util.Context
import co.there4.hexagon.util.asInt
import co.there4.hexagon.util.hostname as utilHostname
import co.there4.hexagon.util.ip as utilIp
import java.lang.System.currentTimeMillis
import java.lang.Thread.currentThread
import java.lang.management.ManagementFactory.getRuntimeMXBean
import java.time.LocalDateTime

open class Event (
    val action: String,
    val timestamp: Long = currentTimeMillis (),
    val dateTime: Long = LocalDateTime.now().asInt(),
    val hostname: String = co.there4.hexagon.util.hostname,
    val ip: String = co.there4.hexagon.util.ip,
    val jvmid: String = getRuntimeMXBean().name,
    val thread: String = currentThread ().name,
    val location: String = currentThread ().stackTrace[3].toString (),
    val context: Map<String, Any> = Context.entries()
        .filter { it.value is String }
        .map { it.key as String to it.value }
        .toMap()
)
