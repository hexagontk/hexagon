package com.hexagonkt

import io.gatling.app.Gatling
import io.gatling.core.config.GatlingPropertiesBuilder

/**
 * This object simply provides a `main` method that wraps
 * [[io.gatling.app.Gatling]].main, which
 * allows us to do some configuration and setup before
 * Gatling launches.
 */
object GatlingRunner {

  def main (args: Array[String]) {

    // This sets the class for the simulation we want to run.
    val simClass = classOf [HexagonSimulation].getName

    val props = new GatlingPropertiesBuilder
    //    props.sourcesDirectory("./src/main/scala")
    //    props.binariesDirectory("./target/scala-2.11/classes")
    props.simulationClass (simClass)
    props.runDescription ("desc")
    props.outputDirectoryBaseName ("base")

    // This checks the values set in gatling_kickoff.rb
    //    if (sys.env("PUPPET_GATLING_REPORTS_ONLY") == "true") {
    //      props.reportsOnly(sys.env("PUPPET_GATLING_REPORTS_TARGET"))
    //    }

    Gatling.fromMap (props.build)
  }
}
