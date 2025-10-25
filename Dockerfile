# ---- Build stage ----
FROM maven:3.9-eclipse-temurin-17 AS build
WORKDIR /app

# Pre-copy pom to leverage docker layer caching
COPY pom.xml .
RUN mvn -q -e -DskipTests dependency:go-offline

# Copy sources and build
COPY checkstyle.xml .
COPY src ./src
RUN mvn -q -DskipTests package

# ---- Runtime stage ----
FROM eclipse-temurin:17-jre
WORKDIR /app

# Copy the fat jar from the build stage
COPY --from=build /app/target/*.jar /app/app.jar

# Copy JaCoCo agent into the image for runtime coverage (from Maven cache)
# Version must match pom.xml <jacoco.version>
COPY --from=build /root/.m2/repository/org/jacoco/org.jacoco.agent/0.8.11/org.jacoco.agent-0.8.11-runtime.jar /app/jacocoagent.jar

# App listens on port 8080
EXPOSE 8080

# Default profile can be overridden by env SPRING_PROFILES_ACTIVE
ENV SPRING_PROFILES_ACTIVE=postgres

# Pass-through DB envs (optional defaults provided by compose)
ENV DB_URL=""
ENV DB_USERNAME=""
ENV DB_PASSWORD=""

ENTRYPOINT ["java","-jar","/app/app.jar"]
