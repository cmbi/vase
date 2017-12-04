FROM tomcat:9.0-jre8

RUN mkdir /usr/src/app
WORKDIR /src/src/app

RUN apt-get update
RUN apt-get install -y openjdk-8-jdk

ARG MAVEN_VERSION=3.5.2
ARG USER_HOME_DIR="/root"
ARG BASE_URL=https://apache.osuosl.org/maven/maven-3/${MAVEN_VERSION}/binaries

RUN mkdir -p /usr/share/maven /usr/share/maven/ref \
  && curl -fsSL -o /tmp/apache-maven.tar.gz ${BASE_URL}/apache-maven-$MAVEN_VERSION-bin.tar.gz \
  && tar -xzf /tmp/apache-maven.tar.gz -C /usr/share/maven --strip-components=1 \
  && rm -f /tmp/apache-maven.tar.gz \
  && ln -s /usr/share/maven/bin/mvn /usr/bin/mvn

ENV MAVEN_HOME /usr/share/maven
ENV MAVEN_CONFIG "$USER_HOME_DIR/.m2"

COPY mvn-entrypoint.sh /usr/local/bin/mvn-entrypoint.sh
RUN chmod 755 /usr/local/bin/mvn-entrypoint.sh
COPY settings-docker.xml /usr/share/maven/ref/

VOLUME "$USER_HOME_DIR/.m2"

RUN /bin/bash -c /usr/local/bin/mvn-entrypoint.sh
COPY . /src/src/app
RUN mvn package -e

RUN cp target/vase-*.war /usr/local/tomcat/webapps/vase.war
