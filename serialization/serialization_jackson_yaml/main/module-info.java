
module com.hexagontk.serialization_jackson_yaml {

    requires transitive com.hexagontk.core;
    requires transitive com.hexagontk.serialization_jackson;
    requires transitive tools.jackson.dataformat.yaml;

    exports com.hexagontk.serialization.jackson.yaml;
}
