FROM openjdk:17-buster

RUN apt-get update && \
    apt install -y libsctp-dev net-tools && \
    apt install -y tcpdump ngrep && \
    apt install -y sudo && \
    apt install -y vim

ARG MODULE=.
ARG COMPONENT=rest-service
ARG VERSION=0.0.1-SNAPSHOT
ARG JAR=$COMPONENT-$VERSION.jar

# Note: these are not passed to the CMD so if you change
# here you have to change in the CMD too (simply couldn't
# get it to work. Even when exporting them as ENV)
ARG FINAL_JAR=server.jar
ARG CONFIG_FILE=config.yml

ARG WORKDIR=/opt/sniceio/$COMPONENT
ARG USER=sniceio
ARG GROUP=sniceio

RUN groupadd -r $GROUP && \
    useradd -r -g $USER $GROUP -G sudo

# Insecure, will remove after testing etc has completed
RUN echo '%sudo ALL=(ALL) NOPASSWD:ALL' >> /etc/sudoers

WORKDIR $WORKDIR

COPY docker-entrypoint.sh /usr/local/bin/docker-entrypoint.sh
RUN chown $USER:$GROUP /usr/local/bin/docker-entrypoint.sh && \
    chmod 755 /usr/local/bin/docker-entrypoint.sh && \
    chown -R $USER:$GROUP $WORKDIR


COPY --chown=$USER:$GROUP $MODULE/target/$JAR $WORKDIR/$FINAL_JAR
COPY --chown=$USER:$GROUP $MODULE/$CONFIG_FILE $WORKDIR/$CONFIG_FILE

EXPOSE 8080/tcp 8081/tcp

USER $USER
ENTRYPOINT ["docker-entrypoint.sh"]
CMD ["java", "-Djava.net.preferIPv4Stack=true", "-jar", "server.jar", "server", "config.yml"]
