
FROM oracle/graalvm-ce:latest
USER root
WORKDIR /build

ADD . /build
RUN ./gradlew jarAll
RUN native-image -jar \
  /build/build/libs/hexagon_benchmark-all*.jar \
  -H:ReflectionConfigurationFiles=reflection.json \
  -H:+JNI \
  -H:Name="Hexagon Benchmark" \
  --static \
  --delay-class-initialization-to-runtime=hexagonBenchmark

FROM scratch
COPY --from=0 /build/hexagon_benchmark /
ENTRYPOINT /hexagon_benchmark
