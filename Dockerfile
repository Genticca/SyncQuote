FROM gradle:9.5.1-jdk21 AS build
WORKDIR /workspace

COPY gradle gradle
COPY gradlew build.gradle settings.gradle ./
COPY src src

RUN chmod +x gradlew && ./gradlew clean bootJar --no-daemon

FROM eclipse-temurin:21-jre
WORKDIR /app

COPY --from=build /workspace/build/libs/*.jar app.jar

EXPOSE 80

ENTRYPOINT ["java", "-jar", "/app/app.jar"]
