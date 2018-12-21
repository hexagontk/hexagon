package com.hexagonkt.benchmark

import io.gatling.app.Gatling
import io.gatling.core.config.GatlingPropertiesBuilder
import org.testng.annotations.Test

@Test class GatlingTest {

  @Test def runGatlingStressTests () {

    val properties = new GatlingPropertiesBuilder
    val buildDir = System.getProperty("buildDir")

    properties.simulationClass (classOf [BenchmarkSimulation].getName)
    properties.resultsDirectory(if (buildDir == null) "build" else buildDir)

    assert(Gatling.fromMap (properties.build) == 0)
  }
}
