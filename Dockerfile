# Stage 1: Build the Application
# Use a robust Maven image with JDK 17
# CRITICAL FIX: Changed the image tag from the non-existent '3.9.6-openjdk-17-slim'
FROM maven:3-openjdk-17 AS build 
WORKDIR /app
COPY . /app
# Package the application into a JAR file, skipping tests
RUN mvn clean package -DskipTests

# Stage 2: Create the Final Runtime Image
# Use a lightweight OpenJDK image (JRE only) for the final running application
FROM openjdk:17-jdk-slim
# Set the current working directory in the container
WORKDIR /app

# Copy the built JAR file from the 'build' stage
# IMPORTANT: The JAR file name is automatically found if there is only one JAR in /target
COPY --from=build /app/target/*.jar app.jar

# Expose the default port for Spring Boot
EXPOSE 8080

# Define the command to run the application
ENTRYPOINT ["java", "-jar", "app.jar"]