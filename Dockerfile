FROM openjdk:17-alpine
ARG JAR_FILE=target/scala-2.13/*.jar
COPY ${JAR_FILE} registry.jar
ENTRYPOINT ["java","-jar","/registry.jar"]
