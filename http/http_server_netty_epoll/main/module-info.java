
module com.hexagontk.http_server_netty_epoll {

    requires transitive com.hexagontk.http_server_netty;

    requires io.netty.buffer;
    requires io.netty.codec;
    requires io.netty.codec.http;
    requires io.netty.handler;
    requires io.netty.transport;
    requires io.netty.transport.classes.epoll;

    exports com.hexagontk.http.server.netty.epoll;
}
