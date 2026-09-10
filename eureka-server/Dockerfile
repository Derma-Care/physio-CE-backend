# ----------- Stage 1: Build -----------
FROM maven:3.9.9-eclipse-temurin-17 AS build
WORKDIR /app

# Copy Maven project files
COPY pom.xml .
RUN mvn dependency:go-offline

# Copy application source code
COPY src ./src

# Build the application
RUN mvn clean package -DskipTests


# ----------- Stage 2: Run -----------
FROM eclipse-temurin:17-jre-jammy
WORKDIR /app

# Add non-root user
RUN useradd -m appuser
USER appuser

# Copy built JAR from the build stage
COPY --from=build /app/target/eureka-server-0.0.1-SNAPSHOT.jar /app/eureka-server.jar

# Expose the Eureka server port
EXPOSE 8761

# Run the Eureka server
ENTRYPOINT ["java", "-jar", "eureka-server.jar"]
