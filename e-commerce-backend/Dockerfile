# Sử dụng JRE bản nhẹ nhất để tiết kiệm RAM
FROM eclipse-temurin:17-jre-jammy

WORKDIR /app

# Copy file jar đã build sẵn từ máy bạn vào image
# Lưu ý: Thay tên file .jar đúng với tên trong thư mục target của bạn
COPY target/*.jar app.jar

EXPOSE 8080

# Chạy ứng dụng với cấu hình tối ưu RAM thấp
ENTRYPOINT ["java", "-Xms128m", "-Xmx300m", "-jar", "app.jar"]