FROM adoptopenjdk/openjdk8-openj9:alpine-slim

COPY target/demo-enghouse.jar /

ENTRYPOINT ["java", "-jar", "demo-enghouse.jar" ]
