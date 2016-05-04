#!/bin/bash

fw_depends java8

export JAVA_HOME=/opt/java8
gradlew clean assemble
${JAVA_HOME}/bin/java -jar target/hexagon-1.0.0.jar &
