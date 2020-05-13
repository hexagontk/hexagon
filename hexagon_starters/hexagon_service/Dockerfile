
FROM openjdk:11
LABEL description="${description}"
USER root

# Machine setup
ENV PROJECT ${projectName}
ENV TZ UTC
ENV PORT 9090

# Machine setup
EXPOSE \$PORT
RUN ln -snf /usr/share/zoneinfo/\$TZ /etc/localtime && echo \$TZ > /etc/timezone

# Project install
USER 1000
COPY --chown=1000:1000 build/install/\$PROJECT /opt/\$PROJECT

# Process execution
WORKDIR /opt/\$PROJECT
ENTRYPOINT /opt/\$PROJECT/bin/\$PROJECT
