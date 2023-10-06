
module com.hexagonkt.serialization_jackson_yaml {

    requires transitive com.hexagonkt.core;
    requires transitive com.hexagonkt.serialization_jackson;
    requires transitive com.fasterxml.jackson.dataformat.yaml;

    exports com.hexagonkt.serialization.jackson.yaml;
}
