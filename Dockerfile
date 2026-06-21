# Stage 1: build the application
FROM maven:3.9-eclipse-temurin-21 AS builder
WORKDIR /build
COPY pom.xml .
RUN mvn -B dependency:go-offline
COPY src ./src
RUN mvn -B package -DskipTests

# Stage 2: runtime image. This is a one-shot CLI, not a server, so there is no
# default CMD — invoke it via `docker compose run app <command> [--option=value ...]`.
FROM eclipse-temurin:21-jre-alpine
WORKDIR /app
COPY --from=builder /build/target/sql-analyzer.jar app.jar
ENTRYPOINT ["java", "-jar", "/app/app.jar"]
