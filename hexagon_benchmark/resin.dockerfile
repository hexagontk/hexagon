
FROM openjdk
RUN curl http://caucho.com/download/resin-4.0.51.tar.gz | tar xvz -C /opt
COPY build/libs/ROOT.war /opt/resin-4.0.51/webapps
WORKDIR /opt/resin-4.0.51
EXPOSE 8080
ENTRYPOINT [ "/opt/resin-4.0.51/bin/resin.sh" ]
CMD [ "console" ]
