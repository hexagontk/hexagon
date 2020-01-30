rootProject.name = "hexagon"

gradle.rootProject {

    allprojects {
        version = "1.2.1"
        group = "com.hexagonkt"
        description = "The atoms of your platform"

        // Bintray
        val bintrayRepo by extra("maven")
        val licenses by extra("MIT")
        val vcsUrl by extra("https://github.com/hexagonkt/hexagon.git")

        // SonarQube
        val sonarqubeProject by extra("hexagonkt_hexagon")
        val sonarqubeOrganization by extra("hexagonkt")

        // SSL
        val sslOrganization by extra("Hexagon")
        val sslDomain by extra("hexagonkt.com")

        // Site
        val siteHost by extra("https://hexagonkt.com")

        // Relative to hexagon_site
        val logoSmall by extra("assets/img/logo.svg")
        val logoWide by extra("assets/img/logo_wide.svg")
        val logoLarge by extra("assets/img/logo.svg")

        // VERSIONS
        val kotlinVersion by extra("1.3.61")
        val kotlinCoroutinesVersion by extra("1.3.3")

        // hexagon_benchmark
        val hikariVersion by extra("3.4.1")
        val postgresqlVersion by extra("42.2.9")

        // http_server_servlet
        val servletVersion by extra("3.1.0")
        val jettyVersion by extra("9.4.24.v20191120")

        // hexagon_core
        val slf4jVersion by extra("1.7.30")
        val logbackVersion by extra("1.2.3")
        val jacksonVersion by extra("2.10.2")

        // hexagon_scheduler
        val cronutilsVersion by extra("9.0.2")

        // hexagon_web
        val kotlinxHtmlVersion by extra("0.6.12")

        // messaging_rabbitmq
        val rabbitVersion by extra("5.8.0")
        val qpidVersion by extra("6.1.4")

        // http_client_ahc
        val ahcVersion by extra("2.10.4")

        // store_mongodb
        val mongodbVersion by extra("3.12.1")

        // templates_pebble
        val pebbleVersion by extra("3.1.2")

        // Test
        val testngVersion by extra("6.14.3")
        val gatlingVersion by extra("3.3.1")
        val junitVersion by extra("5.6.0")
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
