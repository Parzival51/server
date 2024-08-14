# Java 17 JDK slim taban imajını kullan
FROM openjdk:17-jdk-slim

# Konteyner içinde /app dizinini oluştur ve çalışma dizini olarak ayarla
WORKDIR /app

# build/libs/ dizinindeki JAR dosyasını konteynerin /app dizinine kopyala
COPY build/libs/com.example.ktor-sample-all.jar /app/com.example.ktor-sample-all.jar

# Uygulamayı başlat
CMD ["java", "-jar", "/app/com.example.ktor-sample-all.jar"]
