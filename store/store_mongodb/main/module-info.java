
module com.hexagontk.store.mongodb {

    requires transitive com.hexagontk.core;
    requires transitive com.hexagontk.store;

    requires org.mongodb.driver.sync.client;

    exports com.hexagontk.store.mongodb;
}
