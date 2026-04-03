FROM eclipse-temurin:17-jre-jammy
WORKDIR /app
# Đảm bảo đường dẫn là targets/
COPY targets/*.jar app.jar 
EXPOSE 8080
# Với file 173MB, hãy tăng RAM tối đa cho Java lên một chút (ví dụ 400MB) 
# nhưng phải nằm trong giới hạn của Railway (thường là 512MB)
ENTRYPOINT ["java", "-Xms128m", "-Xmx400m", "-jar", "app.jar"]