
import com.hexagonkt.http.server.HttpServer
import com.hexagonkt.http.server.HttpServerSettings
import com.hexagonkt.http.server.netty.NettyAdapter

fun main() {
    val s = HttpServer(NettyAdapter(), HttpServerSettings(bindPort = 0)) {}
    s.start()
//    s.stop()

    listOf(
        "java.version",
        "java.version.date",
        "java.vendor",
        "java.vendor.url",
        "java.vendor.version",
        "java.home",
        "java.vm.specification.version",
        "java.vm.specification.vendor",
        "java.vm.specification.name",
        "java.vm.version",
        "java.vm.vendor",
        "java.vm.name",
        "java.specification.version",
        "java.specification.vendor",
        "java.specification.name",
        "java.class.version",
//        "java.class.path",
        "java.library.path",
        "java.io.tmpdir",
        "java.compiler",
    )
    .forEach { println("$it: " + System.getProperty(it)) }
}
