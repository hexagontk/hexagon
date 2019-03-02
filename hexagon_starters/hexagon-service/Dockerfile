
FROM openjdk:11
LABEL description="${description}"
USER root

# Machine setup
ENV PROJECT ${projectName}
ENV TZ UTC

VOLUME /tmp
EXPOSE ${bindPort}

RUN ln -snf /usr/share/zoneinfo/\$TZ /etc/localtime && echo \$TZ > /etc/timezone

# Project install
COPY build/install/\$PROJECT /opt/\$PROJECT
RUN touch /opt/\$PROJECT

# Process execution
WORKDIR /opt/\$PROJECT
ENTRYPOINT /opt/\$PROJECT/bin/\$PROJECT
