FROM eclipse-temurin:25-jdk AS build

WORKDIR /app

COPY gradlew .
COPY gradle gradle
COPY build.gradle .
COPY settings.gradle .

RUN chmod +x gradlew

COPY src src

RUN ./gradlew bootJar --no-daemon

RUN cp $(find build/libs -name "*.jar" ! -name "*-plain.jar" | head -n 1) app.jar


FROM eclipse-temurin:25-jre

WORKDIR /app

COPY --from=build /app/app.jar app.jar

EXPOSE 5000

ENTRYPOINT ["java", "-jar", "app.jar"]
