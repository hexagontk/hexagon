
module com.hexagonkt.serialization_jackson {

    requires transitive kotlin.stdlib;
    requires transitive com.fasterxml.jackson.databind;
    requires transitive com.hexagonkt.serialization;

    exports com.hexagonkt.serialization.jackson;
}
