FROM eclipse-temurin:17-jre-jammy
WORKDIR /app

# Thêm một dòng comment bất kỳ để phá cache
# Update timestamp: 123456

# Quan trọng: Đảm bảo RAILWAY_GIT_LFS_ENABLED = true đã được set trong Variables
COPY targets/*.jar app.jar

# Log này phải hiện ~177M thì mới chạy được
RUN ls -lh app.jar

EXPOSE 8080
ENTRYPOINT ["java", "-Xms128m", "-Xmx512m", "-Dfile.encoding=UTF-8", "-jar", "app.jar"]
