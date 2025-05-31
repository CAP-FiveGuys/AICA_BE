FROM amazoncorretto:11-alpine-jdk

ARG PROFILES
ARG ENV

COPY build/libs/*.jar app.jar

ENTRYPOINT ["java", "-Dspring.profiles.active=${PROFILES}", "-Dserver.env=${ENV}", "-jar", "app.jar"]
