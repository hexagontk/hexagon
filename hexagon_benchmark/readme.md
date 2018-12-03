
# Hexagon Benchmark

This is the Hexagon portion of a [benchmarking test suite](../) comparing a variety of web
development platforms. The test utilizes Hexagon routes, serialization and database access.

## Docker Compose

Services needed for benchmarking. This file will not run on its own, it needs to be started after
root's `docker-compose .yaml`. To do so, execute the following command from the root directory:

    docker-compose -f docker-compose.yaml -f hexagon_benchmark/docker-compose.yaml up -d

## Gatling
  
by System.getProperty:

  private val host: String = property("host", "127.0.0.1")
  private val port: Int = property("port", "0").toInt
  private val databaseEngine: String = property("databaseEngine", "mongodb")
  private val templateEngine: String = property("templateEngine", "pebble")
  private val users: Int = property("users", "256").toInt
  private val protocol: String = property("protocol", "http")
  private val count: Int = property("count", "32").toInt

The reports output is generated in:

    val buildDir = System.getProperty("buildDir")

    properties.simulationClass (classOf [BenchmarkSimulation].getName)
    properties.resultsDirectory(if (buildDir == null) "build" else buildDir)

## Benchmark module generation


