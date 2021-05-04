FROM adoptopenjdk/openjdk8-openj9:alpine-slim

COPY target/demo.jar /

RUN mkdir /root/.postgresql/

COPY root.crt /root/.postgresql/

ENTRYPOINT ["java", "-jar", "demo.jar" ]
