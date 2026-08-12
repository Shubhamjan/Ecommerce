# ============================
# Stage 1: Build the application
# ============================
FROM maven:3.9.9-eclipse-temurin-21 AS builder

WORKDIR /app

# Copy pom.xml
COPY pom.xml .

# Download dependencies
RUN mvn dependency:go-offline

# Copy source code
COPY src ./src

# Build the application
RUN mvn clean package -DskipTests

# ============================
# Stage 2: Create runtime image
# ============================
FROM eclipse-temurin:21-jre

WORKDIR /app

# Copy the generated JAR
COPY --from=builder /app/target/*.jar app.jar

# Eureka Server runs on port 8761
EXPOSE 8761

# Start the application
ENTRYPOINT ["java", "-jar", "app.jar"]