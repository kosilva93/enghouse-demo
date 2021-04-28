FROM adoptopenjdk/openjdk8-openj9:alpine-slim

COPY target/demo.jar /

ENTRYPOINT ["java", "-jar", "demo.jar" ]