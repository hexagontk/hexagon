
module com.hexagonkt.web {

    requires transitive kotlin.stdlib;
    requires transitive com.hexagonkt.http;
    requires transitive com.hexagonkt.http_server;
    requires transitive com.hexagonkt.templates;

    exports com.hexagonkt.web;
}
