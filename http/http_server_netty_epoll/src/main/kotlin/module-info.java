
module com.hexagonkt.http_server_netty_epoll {

    requires transitive com.hexagonkt.http_server_netty;

    requires io.netty.buffer;
    requires io.netty.codec;
    requires io.netty.codec.http;
    requires io.netty.handler;
    requires io.netty.transport;
    requires io.netty.transport.classes.epoll;

    exports com.hexagonkt.http.server.netty.epoll;
}
