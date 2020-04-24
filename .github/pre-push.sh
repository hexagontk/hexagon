#!/bin/sh

#
# Refer to `.git/hooks/pre-push.sample` for more information about this hook script.
# Only volumes are cleared. To clean all Docker artifacts execute: `sudo docker system prune -af`
#

set -e

alias dc='docker-compose --log-level warning'
alias gw='./gradlew --warn --quiet --console plain'
alias d='docker --log-level warning'

# Package libraries and examples prior to Docker image generation
gw clean installDist

# Start containers required for tests and benchmarks
# shellcheck disable=SC2034
export COMPOSE_FILE="docker-compose.yml:hexagon_benchmark/docker-compose.yml"
dc rm -sfv
dc up -d --build

# Run all tests
gw all

# Generate documentation
me="$(whoami)"
user="$(id -u "$me"):$(id -g "$me")"
d run --rm -v "$PWD/hexagon_site:/docs" -u "$user" "squidfunk/mkdocs-material:4.6.3" build -sq

# Check publishing
gw publish -PbintrayDryRun=true

# Clean up
d volume prune -f
d system prune -f
