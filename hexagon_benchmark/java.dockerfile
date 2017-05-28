
FROM openjdk
COPY build/install/hexagon_benchmark /opt/hexagon_benchmark
WORKDIR /opt/hexagon_benchmark
EXPOSE 9090
ENTRYPOINT ["/opt/hexagon_benchmark/bin/hexagon_benchmark"]
