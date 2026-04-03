# Stage 1: Build
FROM maven:3.8.5-openjdk-17 AS build
WORKDIR /app

# Bước 1: Chỉ copy file pom.xml để tải thư viện trước (tận dụng cache)
COPY pom.xml .
RUN export MAVEN_OPTS="-Xmx256m" && mvn dependency:go-offline -B

# Bước 2: Sau khi tải xong thư viện mới copy toàn bộ code vào để build
COPY . .
RUN export MAVEN_OPTS="-Xmx256m" && mvn clean package -B -DskipTests

# Stage 2: Run
FROM eclipse-temurin:17-jre-jammy
WORKDIR /app
COPY --from=build /app/target/*.jar app.jar

EXPOSE 8080

# Chạy app với giới hạn RAM chặt chẽ
ENTRYPOINT ["java", "-Xms128m", "-Xmx300m", "-jar", "app.jar"]
