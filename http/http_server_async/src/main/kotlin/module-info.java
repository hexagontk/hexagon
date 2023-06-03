
module com.hexagonkt.http_server {

    requires transitive com.hexagonkt.core;
    requires transitive com.hexagonkt.http_handlers_async;

    exports com.hexagonkt.http.server.async;
    exports com.hexagonkt.http.server.async.callbacks;
}
