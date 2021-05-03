FROM adoptopenjdk/openjdk11:jdk-11.0.9.1_1-alpine-slim

ARG JAR_FILE=target/tetris-vaadin.jar

VOLUME /tmp
COPY ${JAR_FILE} app.jar
ENTRYPOINT ["java","-jar","/app.jar"]