
module com.hexagontk.http_server_netty_io_uring {

    requires transitive com.hexagontk.http_server_netty;

    requires io.netty.transport.unix.common;
    requires io.netty.transport.classes.io_uring;

    exports com.hexagontk.http.server.netty.io.uring;
}
