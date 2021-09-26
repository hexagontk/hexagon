
rootProject.name = "hexagon"

include(
    // Infrastructure
    "hexagon_site",
    "hexagon_starters",

    // Internal modules
    "hexagon_core",
    "hexagon_http",
    "hexagon_web",

    // Ports
    "port_http_client",
    "port_http_server",
    "port_templates",

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
