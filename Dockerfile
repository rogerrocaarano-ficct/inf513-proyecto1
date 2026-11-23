# Image of the application, built by Dokploy from compose.staging.yaml (test environment).
# The automated tests are not run here: they need Docker (Testcontainers) and run before merging to develop.

FROM eclipse-temurin:25-jdk AS build
WORKDIR /workspace

# Wrapper and build files first, so the dependency layer is reused while they do not change.
COPY gradlew settings.gradle.kts build.gradle.kts ./
COPY gradle gradle
RUN chmod +x gradlew && ./gradlew dependencies --no-daemon > /dev/null

COPY src src
# bootJar writes the executable JAR; the plain JAR of the jar task is skipped.
RUN ./gradlew bootJar --no-daemon \
    && cp "$(ls build/libs/*.jar | grep -v -- '-plain.jar$')" app.jar

FROM eclipse-temurin:25-jre
RUN groupadd --system cpms && useradd --system --gid cpms --home-dir /app cpms \
    && mkdir -p /app /var/lib/cpms/storage \
    && chown -R cpms:cpms /app /var/lib/cpms
WORKDIR /app
COPY --from=build --chown=cpms:cpms /workspace/app.jar app.jar
USER cpms

# Files kept by the application (cpms.storage.root of the staging profile). No VOLUME: the test environment
# is disposable and compose.staging.yaml mounts a tmpfs here.

ENTRYPOINT ["java", "-jar", "/app/app.jar"]
