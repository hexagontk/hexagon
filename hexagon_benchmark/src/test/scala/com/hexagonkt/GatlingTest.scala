package com.hexagonkt

import io.gatling.app.Gatling
import io.gatling.core.config.GatlingPropertiesBuilder
import org.testng.annotations.Test

@Test class GatlingTest {

  @Test def runGatlingStressTests () {

    val props = new GatlingPropertiesBuilder
    props.simulationClass (classOf [HexagonSimulation].getName)
    props.outputDirectoryBaseName ("gatling")
    props.resultsDirectory("build")

    Gatling.fromMap (props.build)
  }
}
