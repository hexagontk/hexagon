
module com.hexagontk.serialization_jackson_toml {

    requires transitive com.hexagontk.core;
    requires transitive com.hexagontk.serialization_jackson;
    requires transitive com.fasterxml.jackson.dataformat.toml;

    exports com.hexagontk.serialization.jackson.toml;
}
