
module com.hexagontk.messaging_rabbitmq {

    requires transitive com.hexagontk.helpers;
    requires transitive com.hexagontk.http;
    requires transitive com.hexagontk.messaging;
    requires transitive com.hexagontk.serialization;

    requires com.rabbitmq.client;

    exports com.hexagontk.messaging.rabbitmq;
}
