# Use Maven for building the application
FROM maven:3.8.5-openjdk-17 AS build

# Copy the entire project
COPY . .

# Build the project and skip tests for faster build time
RUN mvn clean package -DskipTests

# Use JDK to run the built application
FROM openjdk:17.0.1-jdk-slim

# Copy the packaged jar from the build stage
COPY --from=build /target/demo-bank-0.0.1-SNAPSHOT.jar demo.jar

# Expose the application port
EXPOSE 8080

# Run the application
ENTRYPOINT ["java", "-jar", "./demo.jar"]
