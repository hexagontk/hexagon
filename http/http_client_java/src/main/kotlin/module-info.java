
module com.hexagonkt.http_client_java {

    requires transitive com.hexagonkt.http;
    requires transitive com.hexagonkt.http_client;
    requires transitive java.net.http;

    exports com.hexagonkt.http.client.java;
}
