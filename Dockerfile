# Stage 1: Build the Application
# Use a Maven image with JDK 17 to build the project
FROM maven:3.9.6-openjdk-17-slim AS build
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
# The name of the JAR file comes from your pom.xml (e.g., summarizer-backend-0.0.1-SNAPSHOT.jar)
# IMPORTANT: Check your target directory to confirm the exact JAR file name!
COPY --from=build /app/target/*.jar app.jar

# Expose the default port for Spring Boot
EXPOSE 8080

# Define the command to run the application
# This is the command Render will execute to start your service
ENTRYPOINT ["java", "-jar", "app.jar"]