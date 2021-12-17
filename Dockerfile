FROM tomcat:9.0.56-jdk11

RUN mkdir /usr/src/app
WORKDIR /src/src/app

RUN apt update
RUN apt install -y maven

ENV MAVEN_HOME /usr/share/maven
ENV MAVEN_CONFIG "$USER_HOME_DIR/.m3"

COPY mvn-entrypoint.sh /usr/local/bin/mvn-entrypoint.sh
RUN chmod 755 /usr/local/bin/mvn-entrypoint.sh
COPY settings-docker.xml /usr/share/maven/ref/

VOLUME "$USER_HOME_DIR/.m3"

RUN /bin/bash -c /usr/local/bin/mvn-entrypoint.sh
COPY . /src/src/app
RUN mvn package -e

RUN cp target/vase-*.war /usr/local/tomcat/webapps/vase.war
