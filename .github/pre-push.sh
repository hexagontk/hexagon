#!/bin/sh

#
# Refer to `.git/hooks/pre-push.sample` for more information about this hook script.
# To clean Docker artifacts execute: `sudo docker system prune -af`
#

set -e

#
# Package libraries and examples prior to Docker image generation
#
./gradlew --quiet clean installDist -x test

#
# Start containers required for tests and benchmarks
#
# shellcheck disable=SC2034
export COMPOSE_FILE="docker-compose.yml:hexagon_benchmark/docker-compose.yml"
docker-compose --log-level warning rm -sf
docker-compose --log-level warning up -d

#
# Runs all tests
#
./gradlew --console=plain --quiet all

#
# Generate documentation
#
me="$(whoami)"
user="$(id -u "$me"):$(id -g "$me")"
mkdocsImage="squidfunk/mkdocs-material:4.6.3"
docker --log-level warning run --rm -v "$PWD/hexagon_site:/docs" -u "$user" $mkdocsImage build -sq

#
# Clean up
#
docker --log-level warning volume prune -f
