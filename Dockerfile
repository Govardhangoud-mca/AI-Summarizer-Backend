# =============================
# Stage 1: Build the Application
# =============================
# Use Maven with JDK 17 (Eclipse Temurin - actively maintained)
FROM maven:3-eclipse-temurin-17 AS build

WORKDIR /app
COPY pom.xml .
COPY src ./src

# Package the Spring Boot app (skip tests for faster builds)
RUN mvn clean package -DskipTests


# =============================
# Stage 2: Run the Application
# =============================
# Use a lightweight JDK 17 runtime image (Temurin)
FROM eclipse-temurin:17-jdk-jammy

WORKDIR /app

# Copy the built JAR from the build stage
COPY --from=build /app/target/*.jar app.jar

# Expose Spring Boot default port
EXPOSE 8080

# Run the Spring Boot application
ENTRYPOINT ["java", "-jar", "app.jar"]
