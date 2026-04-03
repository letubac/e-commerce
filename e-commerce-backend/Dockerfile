FROM eclipse-temurin:17-jre-jammy
WORKDIR /app

# Vì Dockerfile đang đứng ở e-commerce-backend, 
# nên chỉ cần đi vào thư mục targets/
COPY targets/*.jar app.jar

# Lệnh kiểm tra dung lượng để chắc chắn là file 177MB thật
RUN ls -lh app.jar

EXPOSE 8080

# Chạy với cấu hình RAM tối ưu cho Railway (512MB - 1GB)
ENTRYPOINT ["java", "-Xms128m", "-Xmx512m", "-Dfile.encoding=UTF-8", "-jar", "app.jar"]
