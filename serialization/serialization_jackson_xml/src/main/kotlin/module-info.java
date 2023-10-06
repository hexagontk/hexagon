
module com.hexagonkt.serialization_jackson_xml {

    requires transitive com.hexagonkt.core;
    requires transitive com.hexagonkt.serialization_jackson;
    requires transitive com.fasterxml.jackson.dataformat.xml;

    exports com.hexagonkt.serialization.jackson.xml;
}
