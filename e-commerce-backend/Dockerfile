FROM maven:3.8.5-openjdk-17 AS build
WORKDIR /app
COPY . .
# Giới hạn RAM cho Maven để tránh lỗi 137 trên Railway
RUN export MAVEN_OPTS="-Xmx300m" && mvn -B -DskipTests clean package

# Stage 2: Run
FROM eclipse-temurin:17-jre-jammy
WORKDIR /app
# Copy file jar từ Stage 1 sang
COPY --from=build /app/target/*.jar app.jar

EXPOSE 8080

# Chạy ứng dụng và giới hạn RAM trực tiếp cho Java Runtime
ENTRYPOINT ["java", "-Xmx300m", "-jar", "app.jar"]
