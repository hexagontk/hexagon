
rootProject.name = "hexagon"

include(
    // Infrastructure
    "site",
    "starters",

    // Internal modules
    "core",
    "http",
    "web",

    // Ports
    "http_client",
    "http_server",
    "serialization",
    "templates",

    // Adapters
    "logging_logback",
    "logging_slf4j_jul",
    "serialization_json",
    "serialization_csv",
    "serialization_yaml",
    "serialization_xml",
    "http_client_ahc",
    "http_server_servlet",
    "http_server_jetty",
    "templates_pebble",
    "templates_freemarker"
)
