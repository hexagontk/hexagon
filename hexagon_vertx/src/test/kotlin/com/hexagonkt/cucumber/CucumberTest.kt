package com.hexagonkt.cucumber

import cucumber.api.CucumberOptions
import cucumber.api.junit.Cucumber
import org.junit.runner.RunWith

@RunWith(Cucumber::class)
@CucumberOptions(
    format = [
        "html:build/reports/cucumber",
        "json:build/reports/cucumber/cucumber.json",
        "junit:build/reports/cucumber/cucumber.xml"
    ],
    features = [ "src/test/resources/features" ]
)
class CucumberTest
