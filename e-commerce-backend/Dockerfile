# Stage 1: Build
FROM maven:3.9.2-eclipse-temurin-17 AS build

WORKDIR /app

# Copy pom.xml first (helps caching)
COPY pom.xml .

# Download dependencies
RUN mvn dependency:go-offline

# Copy source code
COPY src ./src

# Build the project
RUN mvn clean package -DskipTests

# Stage 2: Runtime
FROM eclipse-temurin:17-jre-jammy

WORKDIR /app

# Copy jar từ stage build
COPY --from=build /app/target/*.jar app.jar

# Run the application
ENTRYPOINT ["java","-jar","app.jar"]
