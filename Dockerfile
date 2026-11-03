# Build stage: compiles the executable jar with the Gradle wrapper.
FROM eclipse-temurin:25-jdk AS build
WORKDIR /workspace
COPY gradlew settings.gradle.kts build.gradle.kts ./
COPY gradle gradle
RUN ./gradlew --no-daemon dependencies > /dev/null
COPY src src
RUN ./gradlew --no-daemon bootJar

# Runtime stage: only the JRE and the jar.
FROM eclipse-temurin:25-jre
WORKDIR /app
RUN useradd --system --uid 1001 mailapp \
    && mkdir -p /app/data/files \
    && chown mailapp /app/data/files
COPY --from=build /workspace/build/libs/mail-app-*.jar app.jar
USER mailapp
ENTRYPOINT ["java", "-jar", "/app/app.jar"]
