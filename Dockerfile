FROM openjdk:8-jre

RUN apt-get update
RUN apt-get install -y xvfb

COPY target/scylla-0.0.1-SNAPSHOT.jar /opt/scylla/projects/Scylla.jar
COPY target/libs /opt/scylla/projects/libs
COPY webswing.config /opt/scylla/webswing.config
COPY webswing-server.war /opt/scylla/webswing-server.war

COPY docker_entrypoint.sh /docker_entrypoint.sh

CMD /docker_entrypoint.sh
