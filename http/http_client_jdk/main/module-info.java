
module com.hexagontk.http_client_jdk {

    requires transitive com.hexagontk.http;
    requires transitive com.hexagontk.http_client;
    requires transitive java.net.http;

    exports com.hexagontk.http.client.jdk;
}
