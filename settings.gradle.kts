
rootProject.name = "hexagon"

gradle.rootProject {

    allprojects {
        version = "1.2.3"
        group = "com.hexagonkt"
        description = "The atoms of your platform"

        // Bintray
        extra["bintrayRepo"] = "maven"
        extra["licenses"] = "MIT"
        extra["vcsUrl"] = "https://github.com/hexagonkt/hexagon.git"

        // SonarQube
        extra["sonarQubeProject"] = "hexagonkt_hexagon"
        extra["sonarQubeOrganization"] = "hexagonkt"

        // SSL
        extra["sslOrganization"] = "Hexagon"
        extra["sslDomain"] = "hexagonkt.com"

        // Site
        extra["siteHost"] = "https://hexagonkt.com"

        // Relative to hexagon_site
        extra["logoSmall"] = "assets/img/logo.svg"
        extra["logoWide"] = "assets/img/logo_wide.svg"
        extra["logoLarge"] = "assets/img/logo.svg"

        // VERSIONS
        extra["kotlinVersion"] = "1.3.61"
        extra["kotlinCoroutinesVersion"] = "1.3.3"

        // hexagon_benchmark
        extra["hikariVersion"] = "3.4.2"
        extra["postgresqlVersion"] = "42.2.10"

        // http_server_servlet
        extra["servletVersion"] = "3.1.0"
        extra["jettyVersion"] = "9.4.26.v20200117"

        // hexagon_core
        extra["slf4jVersion"] = "1.7.30"
        extra["logbackVersion"] = "1.2.3"
        extra["jacksonVersion"] = "2.10.2"

        // hexagon_scheduler
        extra["cronutilsVersion"] = "9.0.2"

        // hexagon_web
        extra["kotlinxHtmlVersion"] = "0.7.1"

        // messaging_rabbitmq
        extra["rabbitVersion"] = "5.8.0"
        extra["qpidVersion"] = "6.1.4"

        // http_client_ahc
        extra["ahcVersion"] = "2.10.5"

        // store_mongodb
        extra["mongodbVersion"] = "3.12.1"

        // templates_pebble
        extra["pebbleVersion"] = "3.1.2"

        // Test
        extra["testngVersion"] = "6.14.3"
        extra["gatlingVersion"] = "3.3.1"
        extra["junitVersion"] = "5.6.0"
    }
}

include(
    // Infrastructure
    "hexagon_site",
    "hexagon_starters",
    "hexagon_benchmark",

    // Internal modules
    "hexagon_core",
    "hexagon_scheduler",
    "hexagon_web",

    // Ports
    "port_http_client",
    "port_http_server",
    "port_messaging",
    "port_store",
    "port_templates",

    // Adapters
    "messaging_rabbitmq",
    "http_client_ahc",
    "http_server_servlet",
    "http_server_jetty",
    "store_mongodb",
    "templates_pebble"
)
