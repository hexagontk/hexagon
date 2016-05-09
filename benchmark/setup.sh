#!/bin/bash

./gradlew
build/install/hexagon/bin/hexagon >/dev/null 2>/dev/null &
