FROM eclipse-temurin:21-jdk

RUN apt-get update && apt-get install -y \
    libasound2t64 \
    libssl3 \
    libstdc++6 \
    libuuid1

ARG PROFILES
ARG ENV

COPY build/libs/*.jar app.jar

ENTRYPOINT ["java", "-Dspring.profiles.active=${PROFILES}", "-Dserver.env=${ENV}", "-jar", "app.jar"]