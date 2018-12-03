
FROM openjdk:11
LABEL description="Hexagon Benchmark Resin"
USER root

ENV TZ Europe/Madrid
ENV RESIN 4.0.58

# Machine setup
VOLUME /tmp
EXPOSE 8080

RUN ln -snf /usr/share/zoneinfo/$TZ /etc/localtime && echo $TZ > /etc/timezone
RUN curl http://caucho.com/download/resin-$RESIN.tar.gz | tar xvz -C /opt

# Project install
COPY build/libs/ROOT.war /opt/resin-$RESIN/webapps

# Process execution
WORKDIR /opt/resin-$RESIN
ENTRYPOINT /opt/resin-$RESIN/bin/resin.sh console
