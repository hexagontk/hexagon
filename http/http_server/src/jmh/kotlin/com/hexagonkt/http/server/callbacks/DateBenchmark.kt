package com.hexagonkt.http.server.callbacks

import com.hexagonkt.http.model.Header
import com.hexagonkt.http.handlers.HttpContext
import com.hexagonkt.http.toHttpFormat
import org.openjdk.jmh.annotations.Benchmark
import org.openjdk.jmh.annotations.Scope
import org.openjdk.jmh.annotations.State
import org.openjdk.jmh.infra.Blackhole
import java.time.Instant

@State(Scope.Benchmark)
open class DateBenchmark {

    private object SimpleDateCallback : (HttpContext) -> HttpContext {
        override fun invoke(context: HttpContext): HttpContext {
            val header = Header("date", Instant.now().toHttpFormat())
            return context.send(headers = context.response.headers + header).next()
        }
    }

    private val dateCallback: DateCallback = DateCallback()

    @Benchmark fun simpleDateCallback(bh: Blackhole) {
        bh.consume(SimpleDateCallback.invoke(HttpContext()))
    }

    @Benchmark fun dateCallback(bh: Blackhole) {
        bh.consume(dateCallback.invoke(HttpContext()))
    }
}
