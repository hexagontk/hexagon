
module com.hexagontk.http_server_netty {

    requires transitive kotlin.stdlib;
    requires transitive com.hexagontk.http;
    requires transitive com.hexagontk.http_server;

    requires io.netty.common;
    requires io.netty.buffer;
    requires io.netty.codec;
    requires io.netty.codec.http;
    requires io.netty.handler;
    requires io.netty.transport;

    requires static io.netty.transport.classes.epoll;

    requires static io.netty.transport.unix.common;
    requires static io.netty.transport.classes.io_uring;

    exports com.hexagontk.http.server.netty;
    exports com.hexagontk.http.server.netty.epoll;
    exports com.hexagontk.http.server.netty.io.uring;
}
