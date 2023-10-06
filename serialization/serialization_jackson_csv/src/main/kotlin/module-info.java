
module com.hexagonkt.serialization_jackson_csv {

    requires transitive com.hexagonkt.core;
    requires transitive com.hexagonkt.serialization_jackson;
    requires transitive com.fasterxml.jackson.dataformat.csv;

    exports com.hexagonkt.serialization.jackson.csv;
}
