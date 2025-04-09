# ---------- Stage 1: Build ----------
FROM maven:3.8.4-openjdk-17 AS build

WORKDIR /app

# Copy project files
COPY .mvn .mvn
COPY mvnw .
COPY pom.xml .

# ✅ Give execute permission to mvnw
RUN chmod +x mvnw

# Download dependencies (cache layer)
RUN ./mvnw dependency:go-offline

# Copy the rest of the source code
COPY src ./src

# Build the project
RUN ./mvnw clean package -DskipTests

# ---------- Stage 2: Run ----------
FROM openjdk:17-jdk-slim

WORKDIR /app
VOLUME /tmp

# Copy the built JAR from the build stage
COPY --from=build /app/target/*.jar app.jar

EXPOSE 8080
ENTRYPOINT ["java", "-jar", "app.jar"]
