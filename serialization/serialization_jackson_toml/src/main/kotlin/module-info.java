
module com.hexagonkt.serialization_jackson_toml {

    requires transitive com.hexagonkt.core;
    requires transitive com.hexagonkt.serialization_jackson;
    requires transitive com.fasterxml.jackson.dataformat.toml;

    exports com.hexagonkt.serialization.jackson.toml;
}
