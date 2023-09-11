package com.hexagonkt.http.test

import com.hexagonkt.http.model.Header
import com.hexagonkt.http.server.coroutines.callbacks.DateCallback
import com.hexagonkt.http.handlers.coroutines.HttpContext
import com.hexagonkt.http.toHttpFormat
import kotlinx.coroutines.runBlocking
import org.openjdk.jmh.annotations.Benchmark
import org.openjdk.jmh.annotations.Scope
import org.openjdk.jmh.annotations.State
import org.openjdk.jmh.infra.Blackhole
import java.time.Instant

@State(Scope.Benchmark)
open class DateBenchmark {

    private object SimpleDateCallback : suspend (HttpContext) -> HttpContext {
        override suspend fun invoke(context: HttpContext): HttpContext {
            val header = Header("date", Instant.now().toHttpFormat())
            return context.send(headers = context.response.headers + header).next()
        }
    }

    private val dateCallback: DateCallback = DateCallback()

    @Benchmark fun simpleDateCallback(bh: Blackhole) = runBlocking {
        bh.consume(SimpleDateCallback.invoke(HttpContext()))
    }

    @Benchmark fun dateCallback(bh: Blackhole) = runBlocking {
        bh.consume(dateCallback.invoke(HttpContext()))
    }
}
