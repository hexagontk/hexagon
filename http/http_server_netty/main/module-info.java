
module com.hexagontk.http_server_netty {

    requires transitive kotlin.stdlib;
    requires transitive com.hexagontk.http;
    requires transitive com.hexagontk.http_server;

    requires transitive io.netty.common;
    requires transitive io.netty.buffer;
    requires transitive io.netty.codec;
    requires transitive io.netty.codec.http;
    requires transitive io.netty.handler;
    requires transitive io.netty.transport;

    requires static io.netty.transport.unix.common;
    requires static io.netty.transport.classes.epoll;
    requires static io.netty.transport.classes.io_uring;

    exports com.hexagontk.http.server.netty;
    exports com.hexagontk.http.server.netty.epoll;
    exports com.hexagontk.http.server.netty.io.uring;
}
