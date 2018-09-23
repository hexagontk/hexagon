
FROM openjdk:8
ENV RESIN 4.0.58

RUN curl http://caucho.com/download/resin-$RESIN.tar.gz | tar xvz -C /opt
COPY build/libs/ROOT.war /opt/resin-$RESIN/webapps
WORKDIR /opt/resin-$RESIN
EXPOSE 8080
ENTRYPOINT /opt/resin-$RESIN/bin/resin.sh
CMD [ "console" ]
