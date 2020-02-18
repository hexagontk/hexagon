
FROM oracle/graalvm-ce:19.3.1-java11 as build
USER root
WORKDIR /build

ADD ./build/libs/*-all-*.jar /build
RUN gu install native-image
RUN native-image -jar \
  /build/hexagon_benchmark-all*.jar \
  -H:+ReportExceptionStackTraces \
  --no-fallback \
  --static \
  hexagon_benchmark

#RUN native-image -jar \
#  /build/hexagon_benchmark-all*.jar \
#  -H:ReflectionConfigurationFiles=reflection.json \
#  -H:+JNI \
#  -H:Name="Hexagon Benchmark" \
#  --static \
#  --delay-class-initialization-to-runtime=hexagonBenchmark

FROM scratch
COPY --from=build /build/hexagon_benchmark /
ENTRYPOINT /hexagon_benchmark
