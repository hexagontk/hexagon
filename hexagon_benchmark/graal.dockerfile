
FROM birdy/graalvm:latest
USER root
WORKDIR /build

ADD . /build
RUN ./gradlew installDist
RUN native-image -jar \
  /build/build/libs/flyhopper.jar \
  -H:ReflectionConfigurationFiles=reflection.json \
  -H:+JNI \
  -H:Name=flyhopper \
  --static \
  --delay-class-initialization-to-runtime=flyhopper

FROM scratch
COPY --from=0 /build/flyhopper /
ENTRYPOINT /flyhopper
