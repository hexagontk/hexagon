
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

    exports com.hexagontk.http.server.netty;
}
