
module com.hexagonkt.http_server_netty {

    requires transitive kotlin.stdlib;
    requires transitive com.hexagonkt.http_handlers;
    requires transitive com.hexagonkt.http_server;

    exports com.hexagonkt.http.server.netty;
}
