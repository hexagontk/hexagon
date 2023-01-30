package com.hexagonkt.http.test

import com.hexagonkt.handlers.*
import com.hexagonkt.http.model.Header
import com.hexagonkt.http.server.callbacks.DateCallback
import com.hexagonkt.http.server.handlers.HttpServerContext
import com.hexagonkt.http.toHttpFormat
import org.openjdk.jmh.annotations.Benchmark
import org.openjdk.jmh.annotations.Scope
import org.openjdk.jmh.annotations.State
import org.openjdk.jmh.infra.Blackhole
import java.time.Instant

@State(Scope.Benchmark)
open class DateBenchmark {

    private object SimpleDateCallback : (HttpServerContext) -> HttpServerContext {
        override fun invoke(context: HttpServerContext): HttpServerContext {
            val header = Header("date", Instant.now().toHttpFormat())
            return context.send(headers = context.response.headers + header).next()
        }
    }

    private val dateCallback: DateCallback = DateCallback()

    @Benchmark fun simple_date_callback(bh: Blackhole) {
        bh.consume(SimpleDateCallback.invoke(HttpServerContext()))
    }

    @Benchmark fun date_callback(bh: Blackhole) {
        bh.consume(dateCallback.invoke(HttpServerContext()))
    }
}
