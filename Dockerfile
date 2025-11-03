FROM amazoncorretto:21-alpine-jdk

RUN apk add --no-cache alsa-lib openssl-dev libstdc++

ARG PROFILES
ARG ENV

COPY build/libs/*.jar app.jar

ENTRYPOINT ["java", "-Dspring.profiles.active=${PROFILES}", "-Dserver.env=${ENV}", "-jar", "app.jar"]