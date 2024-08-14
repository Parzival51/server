FROM openjdk:17-jdk-slim

WORKDIR /app

# Projenin tüm dosyalarını Docker konteynerine kopyalayın
COPY . /app

# JAR dosyasını oluşturmak için Gradle'ı çalıştırın
RUN ./gradlew buildFatJar

# JAR dosyasını çalıştırın
CMD ["java", "-jar", "/app/build/libs/com.example.ktor-sample-all.jar"]
