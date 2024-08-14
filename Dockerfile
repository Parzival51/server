# Java 17 JDK slim taban imajını kullan
FROM openjdk:17-jdk-slim

# Konteyner içinde /app dizinini oluştur ve çalışma dizini olarak ayarla
WORKDIR /app

# Projenin tüm dosyalarını Docker konteynerine kopyalayın
COPY . /app

# 'gradlew' dosyasına çalıştırma izni verin
RUN chmod +x ./gradlew

# JAR dosyasını oluşturmak için Gradle'ı çalıştırın
RUN ./gradlew buildFatJar

# JAR dosyasını çalıştırın
CMD ["java", "-jar", "/app/build/libs/com.example.ktor-sample-all.jar"]

