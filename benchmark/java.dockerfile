
FROM openjdk
COPY build/install/benchmark /opt/benchmark
WORKDIR /opt/benchmark
EXPOSE 9090
ENTRYPOINT ["/opt/benchmark/bin/benchmark"]
